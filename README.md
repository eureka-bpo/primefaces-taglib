# Eureka-BPO PremiumSchedule
PremiumSchedule component for Primefaces.

In addition to standard Primefaces Schedule possibilities (Outlook Calendar, iCal), Eureka-BPO PremiumSchedule enables to use Vertical Resource View and Timeline View - Premium possibilities of FullCalendar.

**Important**: Need to add [license key](https://fullcalendar.io/license) into `web.xml`:

```xml
<context-param>
	<param-name>primefaces.SCHEDULE_LICENSE_KEY</param-name>
	<param-value>LICENSE KEY HERE</param-value>
</context-param>
```

## Examples
**Vertical Resource View**
![Component in Vertical Resource View mode](/timegrid.png)


**Timeline View**
![Component in Timeline View mode](/timeline.png)
