# Backup & Restore Guidance

## Database

**Backup (logical, daily):**
```bash
mysqldump --single-transaction --routines --triggers \
  -u eforms_app -p eforms_dashboard > eforms_dashboard_$(date +%F).sql
```

**Restore:**
```bash
mysql -u eforms_app -p eforms_dashboard < eforms_dashboard_2026-08-31.sql
```

For larger production volumes, prefer MySQL Enterprise Backup / Percona
XtraBackup for a hot, non-locking physical backup, plus binary log
archiving for point-in-time recovery.

## Attachments

`app.file-storage.root-location` (default `./storage/attachments`) must be
on a volume that is backed up alongside the database — attachment metadata
in `ticket_attachments` refers to files by generated name; losing the
filesystem without the DB backup (or vice versa) leaves orphaned records or
orphaned files. Back up both together in the same backup window.

## Retention

- Database backups: recommended 35-day rolling retention + monthly archive
- Attachment storage: match the database retention window
- `audit_logs` table: never purge without an explicit, approved retention
  policy — this is the compliance trail for the application

## Restore drill checklist

1. Restore database dump to a scratch instance.
2. Restore attachment volume snapshot to a scratch path.
3. Point a scratch deployment (`APP_PROFILE=prod`, non-production DB/URL)
   at the restored data.
4. Verify: login works, a known ticket's timeline/activities/attachments
   are intact, and `audit_logs` row counts are consistent with the backup
   timestamp.
