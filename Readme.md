## Debug
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

### Password
Password that application uses is written in the /opt/PT.Fitness-Api/fintess-api.env
It is created by github action
```
    - name: Configure environment variables and systemd service
      env:
        SERVER_PORT: ${{ matrix.server-port }}
        DB_URL: ${{ matrix.db-url }}
        DB_USER: ${{ matrix.db-user }}
        DB_PASS: ${{ secrets.DB_PASSWORD }}
```

Password on the Cloud is taken from the terraform.tfvars

### Cloud
Resources that were needed:
- cloud sql with PSC connection enabled
- private service connect - I needed to provide url for the sql (projects/oa26f940d25c4019ap-tp/regions/europe-central2/serviceAttachments/a-e1cc43bf2b10-psc-service-attachment-4a053db712101d02
) and the subnetwork 

