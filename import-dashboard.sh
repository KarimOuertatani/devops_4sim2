#!/bin/bash
GRAFANA_URL="http://$(minikube ip):30092"
GRAFANA_USER="admin"
GRAFANA_PASS="admin"

echo "Importing dashboard to Grafana..."
echo "URL: $GRAFANA_URL"

# Importer le dashboard via API
curl -X POST \
  -H "Content-Type: application/json" \
  -d @devops-dashboard.json \
  "$GRAFANA_URL/api/dashboards/db" \
  -u "$GRAFANA_USER:$GRAFANA_PASS" \
  -w "\nHTTP Status: %{http_code}\n"
