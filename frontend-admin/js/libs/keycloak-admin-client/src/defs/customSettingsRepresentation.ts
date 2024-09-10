import JsonResponse from "./jsonResponse.js";
import CustomSettingRepresentation from "./customSettingRepresentation.js";

export default interface CustomSettingsRepresentation extends JsonResponse<{settings: CustomSettingRepresentation[]}> {}
