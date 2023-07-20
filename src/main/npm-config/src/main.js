export * from '@fullcalendar/core';
export * from '@fullcalendar/interaction';
export * from '@fullcalendar/daygrid';
export * from '@fullcalendar/timegrid';
export * from '@fullcalendar/list';
export * from '@fullcalendar/moment';
export * from '@fullcalendar/moment-timezone';
export * from '@fullcalendar/resource-common';
export * from '@fullcalendar/resource-timegrid';
export * from '@fullcalendar/resource-daygrid';
export * from '@fullcalendar/resource-timeline';

import allLocales from "@fullcalendar/core/locales-all"

export {default as interactionPlugin} from '@fullcalendar/interaction';
export {default as dayGridPlugin} from '@fullcalendar/daygrid';
export {default as timeGridPlugin} from '@fullcalendar/timegrid';
export {default as listPlugin} from '@fullcalendar/list';
export {default as momentPlugin} from '@fullcalendar/moment';
export {default as momentTimezonePlugin} from '@fullcalendar/moment-timezone';
export {default as resourceDayGridPlugin} from '@fullcalendar/resource-daygrid';
export {default as resourceTimeGridPlugin} from '@fullcalendar/resource-timegrid';
export {default as resourceTimelinePlugin} from '@fullcalendar/resource-timeline';

export const globalLocales = allLocales;

import './main.css';
