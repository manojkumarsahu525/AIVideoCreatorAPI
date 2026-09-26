Power BI integration

This folder contains Kusto queries you can use to create dashboards in Power BI against Application Insights (via Log Analytics).

Steps to create a Power BI dashboard

1. Ensure Application Insights telemetry is linked to a Log Analytics workspace (see `azure/link-appinsights-loganalytics.sh`).
2. Open Power BI Desktop.
3. Get Data -> Azure -> Azure Monitor (Logs).
4. Sign in and select your Log Analytics workspace tied to Application Insights.
5. Use the queries in `powerbi/queries/` to load tables. Example: open `daily-active-users.kql`, paste into the query editor and run.
6. Build visuals:
   - Daily active users: line chart from `daily-active-users` query.
   - Videos per day: bar/line chart from `videos-per-day` query.
   - Average duration: line chart from `avg-duration`.
   - Subscription revenue: line chart from `subscription-revenue`.

Tips
- Set the date range in the query editor to control period.
- You can schedule refresh in Power BI Service if you configure a gateway and appropriate permissions.

If you want I can provide a sample PBIX template; this requires generating a PBIX file which cannot be done reliably in this environment.
