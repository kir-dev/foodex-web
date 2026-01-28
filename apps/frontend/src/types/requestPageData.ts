import { RequestEntity } from './requests';
import { ShiftEntity } from './shift';

export type RequestPageData = {
  incomingFoodExRequests: RequestEntity[];
  acceptedShifts: ShiftEntity[];
};
