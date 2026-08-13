Check if github action is working on the ububtu

```
sudo systemctl status "actions.runner.*"
logs
sudo journalctl -u fitness-api.service -n 100 --no-pager
```