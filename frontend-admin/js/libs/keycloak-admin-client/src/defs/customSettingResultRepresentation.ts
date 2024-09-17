import { Response } from './jsonResponse.js';
import CustomSettingRepresentation from './customSettingRepresentation.js';

export default interface CustomSettingResultRepresentation
	extends Response<{ setting: CustomSettingRepresentation }, 'httpStatus'> {}
