import JsonResponse from "./jsonResponse.js";
import CustomSettingRepresentation from "./customSettingRepresentation.js";

export default interface CustomSettingResultRepresentation extends JsonResponse<{setting: CustomSettingRepresentation}> {}
