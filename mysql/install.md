# MySQL Installation Guide

## 1. Helm Installation

```bash
# Pull MySQL chart
helm pull bitnami/mysql

# Install MySQL with custom values
helm install mysql bitnami/mysql -f values.yaml
```

## 2. Configuration Files

- [values.yaml](values.yaml) - Helm chart configuration
- [simple.sql](simple.sql) - Sample SQL scripts

## 3. MySQL CDC Example

Complete Java CDC implementation available at:
- [mysql-cdc-example/](mysql-cdc-example/) - Maven project with CDC demo