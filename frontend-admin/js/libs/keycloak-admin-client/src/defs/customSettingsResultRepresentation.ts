import JsonResponse from "./jsonResponse.js";
import CustomSettingRepresentation from "./customSettingRepresentation.js";

export default interface CustomSettingsResultRepresentation extends JsonResponse<{settings: CustomSettingRepresentation[]}> {}
