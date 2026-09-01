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

For DB public IP was enabled ot be able to use cloud-sql-proxy


### Terraform

```
alias terraform="/google/bin/releases/g3terraform/runner_main --base_service_dir=\$(pwd) --tf_label='terraform_1_13_5'"
```

## Debug

### Proxy to databsae - does not work
database:
curl -o /usr/local/google/home/pwujczyk/cloud-sql-proxy https://storage.googleapis.com/cloud-sql-connectors/cloud-sql-proxy/v2.14.0/cloud-sql-proxy.linux.amd64
chmod +x /usr/local/google/home/pwujczyk/cloud-sql-proxy

cloud-sql-proxy pwujczyk-pt:europe-central2:ptfitness --port 5432

gcloud auth application-default login

### Tunnel through vm to debug locally
gcloud compute ssh fitness-vm --zone=europe-central2-a --project=pwujczyk-pt -- -L 5432:10.10.0.2:5432 -N