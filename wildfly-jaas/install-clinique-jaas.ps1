$ErrorActionPreference = 'Stop'

$wildflyRoot = 'H:\logiciel\wildfly-41.0.1.Final'
$projectRoot = 'H:\Projetmedical'
$configDir = Join-Path $wildflyRoot 'standalone\configuration'
$moduleDir = Join-Path $wildflyRoot 'modules\com\mycompany\projetmedical\security\main'
$standalone = Join-Path $wildflyRoot 'bin\standalone.bat'
$war = Join-Path $projectRoot 'target\Projetmedical-1.0-SNAPSHOT.war'
$deploymentDir = Join-Path $wildflyRoot 'standalone\deployments'
$deploymentBackupDir = Join-Path $wildflyRoot 'standalone\deployment-source-backup'
$loginJar = Join-Path $projectRoot 'target\Projetmedical-1.0-SNAPSHOT-wildfly-jaas.jar'
$bcryptJar = Join-Path $projectRoot 'target\wildfly-jaas\jbcrypt-0.4.jar'
$moduleXml = Join-Path $projectRoot 'wildfly-jaas\module.xml'
$jaasConfig = Join-Path $projectRoot 'wildfly-jaas\clinique-jaas.conf'

foreach ($path in @($wildflyRoot, $configDir, $deploymentDir, $standalone, $war, $loginJar, $bcryptJar, $moduleXml, $jaasConfig)) {
    if (-not (Test-Path -LiteralPath $path)) { throw "Chemin requis introuvable : $path" }
}

New-Item -ItemType Directory -Path $moduleDir -Force | Out-Null
$configBackup = Join-Path $configDir 'standalone.xml.before-clinique-jaas'
if (-not (Test-Path -LiteralPath $configBackup)) {
    Copy-Item -LiteralPath (Join-Path $configDir 'standalone.xml') -Destination $configBackup
}

Copy-Item -LiteralPath $loginJar -Destination (Join-Path $moduleDir 'projetmedical-wildfly-jaas.jar') -Force
Copy-Item -LiteralPath $bcryptJar -Destination (Join-Path $moduleDir 'jbcrypt-0.4.jar') -Force
Copy-Item -LiteralPath $moduleXml -Destination (Join-Path $moduleDir 'module.xml') -Force
Copy-Item -LiteralPath $jaasConfig -Destination (Join-Path $configDir 'clinique-jaas.conf') -Force

$standaloneXml = Join-Path $configDir 'standalone.xml'
$xml = [System.Xml.XmlDocument]::new()
$xml.PreserveWhitespace = $true
$xml.Load($standaloneXml)
$ns = [System.Xml.XmlNamespaceManager]::new($xml.NameTable)
$ns.AddNamespace('e', 'urn:wildfly:elytron:19.0')
$domain = $xml.SelectSingleNode("//e:security-domain[@name='CliniqueDomain']", $ns)
$securityRealms = $xml.SelectSingleNode('//e:security-realms', $ns)
if ($null -eq $domain -or $null -eq $securityRealms) { throw 'Configuration Elytron Clinique introuvable dans standalone.xml.' }
$domain.SetAttribute('default-realm', 'CliniqueRealm')
$mappedRealm = $domain.SelectSingleNode("e:realm[@name='CliniqueRealm']", $ns)
if ($null -eq $mappedRealm) { $mappedRealm = $domain.SelectSingleNode('e:realm', $ns) }
if ($null -eq $mappedRealm) { throw 'Mapping de realm introuvable dans CliniqueDomain.' }
$mappedRealm.SetAttribute('name', 'CliniqueRealm')
$jdbcRealm = $securityRealms.SelectSingleNode("e:jdbc-realm[@name='CliniqueRealm']", $ns)
$principalQuery = if ($null -ne $jdbcRealm) { $jdbcRealm.SelectSingleNode('e:principal-query', $ns) } else { $null }
if ($null -eq $principalQuery) { throw 'Royaume JDBC CliniqueRealm introuvable dans standalone.xml.' }
$principalQuery.SetAttribute('sql', "SELECT mot_de_passe_hash, role_code FROM public.utilisateurs WHERE lower(email)=lower(?) AND statut='ACTIF'")
$jaasRealm = $securityRealms.SelectSingleNode("e:jaas-realm[@name='CliniqueJaasRealm']", $ns)
if ($null -eq $jaasRealm) {
    $jaasRealm = $xml.CreateElement('jaas-realm', 'urn:wildfly:elytron:19.0')
    $jaasRealm.SetAttribute('name', 'CliniqueJaasRealm')
    $securityRealms.AppendChild($jaasRealm) | Out-Null
}
$jaasRealm.RemoveAttribute('path')
$jaasRealm.RemoveAttribute('relative-to')
$jaasRealm.SetAttribute('entry', 'CliniqueLogin')
$jaasRealm.SetAttribute('module', 'com.mycompany.projetmedical.security')
$fileElement = $jaasRealm.SelectSingleNode('e:file', $ns)
if ($null -eq $fileElement) {
    $fileElement = $xml.CreateElement('file', 'urn:wildfly:elytron:19.0')
    $jaasRealm.AppendChild($fileElement) | Out-Null
}
$fileElement.SetAttribute('path', 'clinique-jaas.conf')
$fileElement.SetAttribute('relative-to', 'jboss.server.config.dir')
$xml.Save($standaloneXml)

$deploymentName = 'Projetmedical-1.0-SNAPSHOT.war'
$deployedPath = Join-Path $deploymentDir $deploymentName
if ((Test-Path -LiteralPath $deployedPath) -and (Get-Item -LiteralPath $deployedPath).PSIsContainer) {
    New-Item -ItemType Directory -Path $deploymentBackupDir -Force | Out-Null
    $backupName = "$deploymentName.exploded-$(Get-Date -Format 'yyyyMMdd-HHmmss')"
    Move-Item -LiteralPath $deployedPath -Destination (Join-Path $deploymentBackupDir $backupName)
}
if (-not (Test-Path -LiteralPath $deployedPath)) {
    Copy-Item -LiteralPath $war -Destination $deployedPath
}
$deploymentNs = [System.Xml.XmlNamespaceManager]::new($xml.NameTable)
$deploymentNs.AddNamespace('d', 'urn:jboss:domain:community:20.0')
$managedDeployment = $xml.SelectSingleNode("/d:server/d:deployments/d:deployment[@name='$deploymentName']", $deploymentNs)
if ($null -ne $managedDeployment) {
    $managedDeployment.ParentNode.RemoveChild($managedDeployment) | Out-Null
    $xml.Save($standaloneXml)
}
$failedMarker = Join-Path $deploymentDir "$deploymentName.failed"
if (Test-Path -LiteralPath $failedMarker) {
    Remove-Item -LiteralPath $failedMarker -Force
    $deployedMarker = Join-Path $deploymentDir "$deploymentName.deployed"
    if (Test-Path -LiteralPath $deployedMarker) { Remove-Item -LiteralPath $deployedMarker -Force }
}
$deployedMarker = Join-Path $deploymentDir "$deploymentName.deployed"
$deployMarker = Join-Path $deploymentDir "$deploymentName.dodeploy"
if (-not (Test-Path -LiteralPath $deployedMarker) -and -not (Test-Path -LiteralPath $deployMarker)) {
    New-Item -ItemType File -Path $deployMarker | Out-Null
}

Write-Output 'Module JAAS Clinique installé; le scanner WildFly est autorisé à déployer le WAR du projet.'
