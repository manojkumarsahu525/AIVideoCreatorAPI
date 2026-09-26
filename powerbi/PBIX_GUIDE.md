Power BI PBIX Report Guide

This document explains how to create a Power BI report (PBIX) using telemetry and Log Analytics/Kusto queries provided in this repository.

Prerequisites
- Application Insights telemetry linked to a Log Analytics workspace (see `azure/link-appinsights-loganalytics.sh`).
- Power BI Desktop installed.
- Access permissions to the Log Analytics workspace.

Steps to build the PBIX manually
1. Open Power BI Desktop.
2. Get Data -> Azure -> Azure Monitor (Logs).
3. Sign in and select your Azure subscription and Log Analytics workspace connected to Application Insights.
4. In the query editor paste one of the provided Kusto queries (files in `powerbi/queries/`). Examples:
   - `daily-active-users.kql`
   - `videos-per-day.kql`
   - `avg-duration.kql`
   - `subscription-revenue.kql`
5. Load each query as a separate table (e.g., DAU, VideosPerDay, AvgDuration, Revenue).
6. Build visuals:
   - Daily active users: line chart using `DAU` table (Timestamp on X, dau on Y).
   - Videos per day: column chart using `VideosPerDay` (timestamp, videos).
   - Average duration: line chart using `AvgDuration` (timestamp, avgDurationMs).
   - Revenue trends: line chart using `Revenue` table (timestamp, revenue).
7. Add slicers for date range and plan (if you include plan dimension in queries).
8. Format visuals, add titles and tooltips, and arrange on a dashboard page.
9. Save the report as `.pbix`.

Scheduling refresh
- To refresh data in Power BI Service you must publish the PBIX to Power BI Service and configure a data gateway connected to Azure or use DirectQuery if supported.

Exporting visuals
- Once the PBIX is published you can pin visuals to a Power BI dashboard and export images or embed dashboards in other apps.

Notes
- The PBIX file cannot be generated reliably in this environment. Follow the guide to assemble the report locally.
- If you want, I can provide JSON templates of visuals or help with DAX calculations for advanced metrics.
