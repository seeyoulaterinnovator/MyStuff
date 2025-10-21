import Resource from "../resource.js";
import type { KeycloakAdminClient } from "../../client.js";
import type { Response } from "../../defs/jsonResponse.js";
import {
  BrandRepresentation,
  RealmBrandRepresentation,
} from "../../defs/brandRepresentation.js";

type AllBrandsResponse = Response<{
  allBrands: BrandRepresentation[];
}>;

type RealmBrandsResponse = Response<{
  brands: RealmBrandRepresentation[];
  id: string;
}>;

export class CustomBrands extends Resource<{ realm?: string }> {
  public getAllBrands = this.makeRequest<{}, AllBrandsResponse>({
    method: "GET",
    path: "/brands/all",
  });

  public getRealmBrands = this.makeRequest<
    { realm: string; searchRealm?: string },
    RealmBrandsResponse
  >({
    method: "GET",
    path: "/brands",
    urlParamKeys: ["realm"],
    queryParamKeys: ["searchRealm"],
  });

  public setDefaultBrand = this.makeRequest<
    { realm: string; brandId: string },
    Response<RealmBrandRepresentation>
  >({
    method: "PUT",
    path: "/manage-brands/default/{brandId}",
    urlParamKeys: ["realm", "brandId"],
  });

  public addRealmBrand = this.makeRequest<
    { realm: string; brandId: string },
    Response<RealmBrandRepresentation>
  >({
    method: "POST",
    path: "/manage-brands/{brandId}",
    urlParamKeys: ["realm", "brandId"],
  });

  public removeRealmBrand = this.makeRequest<
    { realm: string; brandId: string },
    void
  >({
    method: "DELETE",
    path: "/manage-brands/{brandId}",
    urlParamKeys: ["realm", "brandId"],
  });

  constructor(client: KeycloakAdminClient) {
    super(client, {
      path: "/realms/{realm}",
      getUrlParams: () => ({
        realm: client.realmName,
      }),
      getBaseUrl: () => client.baseUrl,
    });
  }
}
