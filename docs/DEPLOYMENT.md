# Deployment Guide

## Build

```bash
mvn -Pprod clean package -DskipTests
# artifact: target/eforms-dashboard.jar
```

## Option A — Plain Linux (systemd)

```bash
sudo useradd -r -s /bin/false eforms
sudo mkdir -p /opt/eforms/storage/{attachments,templates,import-errors}
sudo cp target/eforms-dashboard.jar /opt/eforms/
sudo chown -R eforms:eforms /opt/eforms
```

`/etc/systemd/system/eforms-dashboard.service`:
```ini
[Unit]
Description=Activation Education EForms Dashboard
After=network.target mysql.service

[Service]
User=eforms
WorkingDirectory=/opt/eforms
Environment=APP_PROFILE=prod
Environment=DB_URL=jdbc:mysql://localhost:3306/eforms_dashboard?useSSL=false&serverTimezone=Asia/Kolkata
Environment=DB_USER=eforms_app
Environment=DB_PASSWORD=REPLACE_ME
Environment=COOKIE_SECURE=true
ExecStart=/usr/bin/java -jar /opt/eforms/eforms-dashboard.jar
Restart=on-failure

[Install]
WantedBy=multi-user.target
```
```bash
sudo systemctl daemon-reload
sudo systemctl enable --now eforms-dashboard
```

## Option B — Docker

`Dockerfile` (included at project root):
```bash
docker build -t eforms-dashboard:1.0.0 .
docker run -d --name eforms-dashboard \
  -e APP_PROFILE=prod \
  -e DB_URL=jdbc:mysql://db-host:3306/eforms_dashboard?useSSL=false&serverTimezone=Asia/Kolkata \
  -e DB_USER=eforms_app -e DB_PASSWORD=REPLACE_ME \
  -e COOKIE_SECURE=true \
  -v /opt/eforms/storage:/app/storage \
  -p 8080:8080 \
  eforms-dashboard:1.0.0
```

## Reverse proxy (recommended)

Terminate TLS at Nginx/Apache and forward to `127.0.0.1:8080`. Ensure the
proxy forwards `X-Forwarded-Proto` so Spring Security's secure-cookie /
HSTS behaviour works correctly behind TLS termination.

## Environment checklist before go-live

- [ ] `DB_URL`, `DB_USER`, `DB_PASSWORD` set via secret manager / systemd
      environment file (never in source control)
- [ ] `COOKIE_SECURE=true` (requires HTTPS)
- [ ] `SEED_*_PASSWORD` variables set for the four named initial accounts,
      **or** the auto-generated one-time passwords captured securely from
      the first-boot log and rotated on first login
- [ ] `FILE_STORAGE_ROOT` pointed at a persistent, backed-up volume
- [ ] `SMTP_*` configured only if email notifications are desired (optional
      — app starts fine without them)
