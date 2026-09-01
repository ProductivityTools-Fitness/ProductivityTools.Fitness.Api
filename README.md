# ProductivityTools.Fitness.Api

REST API service for fitness tracking, workout management, and training schedule logging. Built with **Spring Boot**, **Java 21**, **Spring Data JPA**, **Flyway**, and **PostgreSQL**.

---

## 🏗️ Architecture & Infrastructure

* **Google Cloud Platform (GCP)**:
  * **Compute Engine VM (`fitness-vm`)**: Hosts the Spring Boot application running as a `systemd` service.
  * **Cloud SQL (PostgreSQL 18)**: Managed database instance (`ptfitness`).
  * **Private Service Connect (PSC)**: Provides a private, internal TCP tunnel (`10.10.0.2:5432`) directly from the VPC subnet to Cloud SQL without routing through the public internet.
  * **Public IP (mTLS)**: Enabled on Cloud SQL specifically for **Cloud SQL Auth Proxy** support during local development. Access is strictly protected by Google Cloud IAM and dynamic client certificates.
* **CI/CD Deployment**:
  * Automated GitHub Actions matrix workflow deploying simultaneously to self-hosted runners (`fitness-vm` and local servers).

---

## 💻 Local Development & Connecting to GCP Cloud SQL

If you do not have PostgreSQL installed locally, you can easily connect your local Spring Boot instance to the GCP Cloud SQL database using either **Cloud SQL Auth Proxy** (recommended) or an **SSH Tunnel**.

### Method 1: Cloud SQL Auth Proxy (Recommended)

1. **Install Cloud SQL Proxy**:
   ```bash
   mkdir -p ~/.local/bin
   curl -o ~/.local/bin/cloud-sql-proxy https://storage.googleapis.com/cloud-sql-connectors/cloud-sql-proxy/v2.14.0/cloud-sql-proxy.linux.amd64
   chmod +x ~/.local/bin/cloud-sql-proxy
   ```

2. **Authenticate with Google Cloud**:
   ```bash
   gcloud auth application-default login
   ```

3. **Start the Proxy**:
   ```bash
   cloud-sql-proxy pwujczyk-pt:europe-central2:ptfitness --port 5432
   ```
   *The proxy will listen locally on `127.0.0.1:5432` and forward encrypted traffic to Cloud SQL.*

4. **Run the Spring Boot Application**:
   Your default [`src/main/resources/application.properties`](src/main/resources/application.properties) points to `localhost:5432/ptfitness`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/ptfitness
   spring.datasource.username=postgres
   spring.datasource.password=${DB_PASSWORD:Pawel123}
   ```
   Run the project in your IDE or via command line:
   ```bash
   ./gradlew bootRun
   ```

---

### Method 2: SSH Tunnel via VM

If you prefer not using Cloud SQL Proxy, you can tunnel port 5432 directly through the Compute Engine VM:

```bash
gcloud compute ssh fitness-vm --zone=europe-central2-a --project=pwujczyk-pt -- -L 5432:10.10.0.2:5432 -N
```

---

## 🗄️ Database Schema (Multi-User)

The application supports multi-tenancy and multiple users:

* **`fitness_user`**: Application user profiles and preferences.
* **`exercise`**: Catalogue containing both:
  * **System Exercises (`is_system = TRUE, user_id = NULL`)**: Global, built-in, read-only exercises available to everyone (e.g. Deadlift, Bench Press, Squat).
  * **Custom Exercises (`is_system = FALSE, user_id = <USER_ID>`)**: User-created exercises editable only by their creator.
* **`workout`**: Workout session headers (duration, status, timestamps, notes).
* **`workout_exercise`**: Exercises included in a workout session with order index, rest timer, and notes.
* **`workout_set`**: Sets with weight (kg), repetitions, RPE, set type (`NORMAL`, `WARMUP`, `DROPSET`, `FAILURE`), and completion checkmark.
* **`workout_template` / `workout_template_exercise`**: Routines and workout plans.

Database migrations are managed automatically by **Flyway** under `src/main/resources/db/migration/`.

---

## 🚀 Infrastructure as Code (Terraform)

Terraform configurations are located in `terraform/`:

```bash
cd terraform
terraform init
terraform plan
terraform apply
```

Key resources managed:
* VPC network (`fitness`) and subnetwork in Warsaw (`europe-central2`).
* Network Firewall rules (HTTP port 80 and SSH via IAP).
* Compute Engine VM (`fitness-vm`) with automated GitHub Actions runner setup.
* Cloud SQL PostgreSQL instance with Private Service Connect (PSC) endpoint and Public IP for Proxy access.


## Debug

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