import { Response } from './jsonResponse.js';
import CustomSettingRepresentation from './customSettingRepresentation.js';

export default interface CustomSettingsResultRepresentation
	extends Response<{ settings: CustomSettingRepresentation[] }, 'httpStatus'> {}
