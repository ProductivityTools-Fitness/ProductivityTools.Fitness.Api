<<<<<<< HEAD
=======

## Debug

### Github actoin
>>>>>>> 5d7a621 (readme)
Check if github action is working on the ububtu

```
sudo systemctl status "actions.runner.*"
sudo systemctl status fitness-api.service


logs
sudo journalctl -u fitness-api.service -n 100 --no-pager

sudo systemctl restart fitness-api.service
```

### Posgresql

```
systemctl status postgresql
pg_lsclusters
sudo ufw allow 5432/tcp
```

### Cloud
Resources that were needed:
- cloud sql with PSC connection enabled
- private service connect - I needed to provide url for the sql (projects/oa26f940d25c4019ap-tp/regions/europe-central2/serviceAttachments/a-e1cc43bf2b10-psc-service-attachment-4a053db712101d02
) and the subnetwork 

