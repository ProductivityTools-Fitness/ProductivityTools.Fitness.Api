output "network_id" {
  description = "ID utworzonej sieci VPC"
  value       = google_compute_network.vpc_network.id
}

output "network_name" {
  description = "Nazwa utworzonej sieci VPC"
  value       = google_compute_network.vpc_network.name
}

output "subnetwork_id" {
  description = "ID podsieci w Warszawie"
  value       = google_compute_subnetwork.subnet
}

output "subnetwork_name" {
  description = "Nazwa podsieci w Warszawie"
  value       = google_compute_subnetwork.subnet.name
}

output "subnetwork_region" {
  description = "Region podsieci"
  value       = google_compute_subnetwork.subnet.region
}
