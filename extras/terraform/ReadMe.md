# Hinweise zu Terraform

## Voraussetzungen

- Der Namespace `acme` in Kubernetes ist gemäß der allgemeinen Installationsanleitung
  eingerichtet.
- _Terraform_ ist installiert und über die Umgebungsvariable `PATH` auch aufrufbar
- In `extras/helm/jobconnect/templates/deployment.yaml` müssen die Pfade bei `spec.template.spec.volumes[].hostpath`
  angepasst werden

Ob die Voraussetzungen erfüllt sind kann man beispielsweise folgendermaßen überprüfen:

```shell
    kubectl describe namespace acme

    # PowerShell
    Get-Command terraform
    # macOS
    which terraform

    terraform version
```

## Bereitstellung mit Terraform

```powershell
    cd extras\terraform\jobconnect

    # einmalig und bei Updates von main.tf
    terraform init

    # Ausfuehrungsplan inspizieren
    terraform plan

    # Helm-Chart im Kubernetes-Cluster bereitstellen
    terraform apply -auto-approve
    .\port-forward.ps1

    # Deinstallieren
    terraform destroy -auto-approve
```

Bis der Endpoint für den Service "jobconnect" verfügbar ist, muss man ggf. ein
bisschen warten. Aufgrund der Einstellungen für _Liveness_ und _Readiness_
kann es einige Minuten dauern, bis in der PowerShell angezeigt wird, dass die
Installation erfolgreich war. Mit _Lens_ kann man jedoch die Log-Einträge
inspizieren und so vorher sehen, ob die Installation erfolgreich war. Sobald der
Endpoint verfügbar ist, sieht man in der PowerShell auch die Konsole des
gestarteten (Kubernetes-) Pods.

## Port-Forwarding

Um beim Entwickeln von localhost (und damit von außen) auf einen
Kubernetes-Service zuzugreifen, ist _Port-Forwarding_ die einfachste
Möglichkeit, indem das nachfolgende Kommando für den installierten Service mit
Name _jobconnect_ aufgerufen wird. Alternativ kann auch das Skript `port-forward.ps1`
(s.o.) aufgerufen werden.

```powershell
    kubectl port-forward svc/jobconnect 8443 -n acme
```

## Requests nach erfolgreicher Bereitstellung im Kubernetes-Cluster

### Postman

Nach dem Port-Forwarding kann man z.B. mit _Postman_ auf den in Kubernetes laufenden Service zugreifen.

### curl

Falls _cURL_ installiert ist, lautet der Aufruf:

```powershell
    curl --verbose --user admin:p http://localhost:8443/rest/00000000-0000-0000-0000-000000000001
```

### Invoke-WebRequest

Mit _Invoke-WebRequest_ von der PowerShell ist z.B. folgender Aufruf denkbar:

```powershell
    $response = Invoke-WebRequest https://localhost:8443/rest/00000000-0000-0000-0000-000000000001 `
       -SslProtocol Tls13 -HttpVersion 2 -SkipCertificateCheck -Headers @{Accept = 'application/json'}
    Write-Output $response.RawContent
```

## Dokumentation zur Konfiguration von Terraform

Zur Konfiguration des Terraform-Projekts kann man sich eine _Markdown_-Datei
gemäß der Datei `.terraform-docs.yml` generieren lassen:

```powershell
    terraform-docs .
```

## ClusterIP statt Port-Forwarding

Statt Port-Forwarding kann man auch in `extras\helm\jobconnect\templates` in
der Datei `service.yaml` beim Schlüssel `spec.type` den Wert `LoadBalancer`eintragen
und damit den Defaultwert `ClusterIP` überschreiben. Dadurch öffnet man allerdings
den Service für Zugriffe von außerhalb des Clusters, was eigentlich einem Gateway
vorbehalten sein sollte.
