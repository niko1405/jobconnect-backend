# https://docs.microsoft.com/en-us/powershell/scripting/developer/cmdlet/approved-verbs-for-windows-powershell-commands?view=powershell-7

# Aufruf:   .\port-forward.ps1
# ggf. vorher:  Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
# oder:         Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope CurrentUser

# "Param" muss in der 1. Zeile sein
Param (
    [string]$service = 'jobconnect'
)

Set-StrictMode -Version Latest

$versionMinimum = [Version]'7.6.0'
$versionCurrent = $PSVersionTable.PSVersion
if ($versionMinimum -gt $versionCurrent) {
    throw "PowerShell $versionMinimum statt $versionCurrent erforderlich"
}

# Titel setzen
$host.ui.RawUI.WindowTitle = "$service port forward"

$namespace = 'acme'
kubectl port-forward service/$service 8443 --namespace $namespace
