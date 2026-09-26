Power BI visual templates and tips

This file provides a JSON-like template and suggestions for building visuals in Power BI based on the Kusto queries.

Visual: Daily Active Users (Line Chart)
- Data source: `daily-active-users` query (columns: Timestamp, dau)
- X axis: Timestamp (date)
- Y axis: dau (count distinct users)
- Format: set continuous X-axis, enable data labels, set tooltip to show date and dau.

Visual: Videos Generated Per Day (Column Chart)
- Data source: `videos-per-day` query (timestamp, videos)
- X axis: timestamp, Y axis: videos

Visual: Average Generation Time (Line Chart)
- Data source: `avg-duration` query (timestamp, avgDurationMs)
- Y axis format: milliseconds — consider converting to seconds in DAX or query

Visual: Subscription Revenue (Line Chart)
- Data source: `subscription-revenue` query (timestamp, revenue)
- Y axis currency format

Suggested Layout
- Top row: DAU line chart (left), Videos per day (right)
- Middle row: Average generation time (full width)
- Bottom row: Subscription revenue and a slicer for date range and plan

Power BI JSON visual configuration (example snippet)
{
  "visualType": "lineChart",
  "title": "Daily Active Users",
  "dataFields": {
    "x": "Timestamp",
    "y": "dau"
  },
  "format": {
    "xAxisType": "continuous",
    "yAxisLabel": "Users"
  }
}

Note: Power BI visuals are primarily created via the Desktop UI. Use these templates as a reference when composing visuals.
