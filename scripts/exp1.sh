#!/bin/bash
# experiment 1: stop the service for 180s and log the times
LOG=~/experiment-1.log
sudo -v
echo "$(date -u +%FT%TZ)  STOP" | tee -a $LOG
sudo systemctl stop trackwise.service
sleep 180
echo "$(date -u +%FT%TZ)  START" | tee -a $LOG
sudo systemctl start trackwise.service
until curl -sf -o /dev/null http://localhost:8080/v1/track; do sleep 1; done
echo "$(date -u +%FT%TZ)  APP ANSWERING" | tee -a $LOG
