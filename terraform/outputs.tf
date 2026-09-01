output "network_id" {
  description = "ID of the created VPC network"
  value       = google_compute_network.vpc_network.id
}

output "network_name" {
  description = "Name of the created VPC network"
  value       = google_compute_network.vpc_network.name
}

output "subnetwork_id" {
  description = "ID of the subnetwork in Warsaw"
  value       = google_compute_subnetwork.subnet.id
}

output "subnetwork_name" {
  description = "Name of the subnetwork in Warsaw"
  value       = google_compute_subnetwork.subnet.name
}

output "subnetwork_region" {
  description = "Region of the subnetwork"
  value       = google_compute_subnetwork.subnet.region
}

output "firewall_policy_id" {
  description = "ID of the created Network Firewall Policy"
  value       = google_compute_network_firewall_policy.fitness_basic_access.id
}

output "firewall_policy_name" {
  description = "Name of the created Network Firewall Policy"
  value       = google_compute_network_firewall_policy.fitness_basic_access.name
}

output "instance_name" {
  description = "Name of the virtual machine"
  value       = google_compute_instance.fitness_vm.name
}

output "instance_internal_ip" {
  description = "Internal IP address of the virtual machine"
  value       = google_compute_instance.fitness_vm.network_interface[0].network_ip
}

output "instance_external_ip" {
  description = "Public external IP address of the virtual machine"
  value       = google_compute_instance.fitness_vm.network_interface[0].access_config[0].nat_ip
}

output "instance_web_url" {
  description = "URL to access the web page hosted on the VM"
  value       = "http://${google_compute_instance.fitness_vm.network_interface[0].access_config[0].nat_ip}"
}

output "db_instance_name" {
  description = "Name of the Cloud SQL PostgreSQL instance"
  value       = google_sql_database_instance.postgres.name
}

output "db_connection_name" {
  description = "Connection name for Cloud SQL instance"
  value       = google_sql_database_instance.postgres.connection_name
}

output "db_psc_service_attachment" {
  description = "Service attachment link of the Cloud SQL PSC instance"
  value       = google_sql_database_instance.postgres.psc_service_attachment_link
}

output "db_psc_ip" {
  description = "Internal IP address of the Cloud SQL PSC endpoint inside your Warsaw subnetwork"
  value       = google_compute_address.db_psc_ip.address
}

output "db_jdbc_url" {
  description = "Suggested Spring Boot JDBC URL using PSC endpoint IP inside Warsaw subnet"
  value       = "jdbc:postgresql://${google_compute_address.db_psc_ip.address}:5432/${google_sql_database.database.name}"
}

output "dev_db_instance_name" {
  description = "Name of the Development Cloud SQL PostgreSQL instance"
  value       = google_sql_database_instance.postgres_dev.name
}

output "dev_db_connection_name" {
  description = "Connection name for Dev Cloud SQL instance (used by Cloud SQL Auth Proxy)"
  value       = google_sql_database_instance.postgres_dev.connection_name
}

output "dev_db_public_ip" {
  description = "Public IP address of the Dev Cloud SQL instance"
  value       = google_sql_database_instance.postgres_dev.public_ip_address
}
