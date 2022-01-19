package ru.alamics.sso.keycloak.cities.model;


import java.util.Arrays;

public enum RegionCities {

    RU_ALT("RU-ALT", "Барнаул"),
    RU_BRY("RU-ALT", "Брянск"),
    RU_VGG("RU-VGG", "Волгоград"),
    RU_VOR("RU-VOR", "Воронеж"),
    RU_KIR("RU-KIR", "Киров"),
    RU_KYA("RU-KYA", "Красноярск"),
    RU_LIP("RU-LIP", "Липецк"),
    RU_NIZ("RU-NIZ", "Нижний Новгород"),
    RU_NVS("RU-NVS", "Новосибирск"),
    RU_OMS("RU-OMS", "Омск"),
    RU_ORE("RU-ORE", "Оренбург"),
    RU_PNZ("RU-PNZ", "Пенза"),
    RU_PER("RU-PER", "Пермь"),
    RU_ME("RU-ME", "Йошкар-Ола"),
    RU_TA("RU-TA", "Казань"),
    RU_MOW("RU-MOW", "Москва"),
    RU_MOS("RU-MOS", "Москва"),
    RU_KDA("RU-KDA", "Краснодар"),
    RU_TAM("RU-TAM", "Мичуринск"),
    RU_ROS("RU-ROS", "Ростов-на-Дону"),
    RU_BU("RU-BU", "Улан-Удэ"),
    RU_LEN("RU-LEN", "Сосновый Бор"),
    RU_UD("RU-UD", "Ижевск"),
    RU_CU("RU-CU", "Чебоксары"),
    RU_RYA("RU-RYA", "Рязань"),
    RU_SAM("RU-SAM", "Самара"),
    RU_SPE("RU-SPE", "Санкт-Петербург"),
    RU_SAR("RU-SAR", "Саратов"),
    RU_SVE("RU-SVE", "Екатеринбург"),
    RU_TYU("RU-TYU", "Тюмень"),
    RU_CHE("RU-CHE", "Челябинск"),
    RU_YAR("RU-YAR", "Ярославль"),
    RU_TUL("RU-TUL", "Тула"),
    RU_IRK("RU-IRK", "Иркутск"),
    RU_TOM("RU-TOM", "Томск"),
    RU_KGN("RU-KGN", "Курган"),
    RU_KRS("RU-KRS", "Курск"),
    RU_TVE("RU-TVE", "Тверь"),
    RU_BA("RU-BA", "Уфа"),
    RU_ULY("RU-ULY", "Ульяновск");

    private final String region;
    private final String defaultCity;

    RegionCities(String region, String defaultCity) {
        this.region = region;
        this.defaultCity = defaultCity;
    }

    public static RegionCities findRegionByIsoCode(String isoCode) {
        return Arrays.stream(RegionCities.values())
                .filter(regionCities -> regionCities.getRegion().equals(isoCode))
                .findFirst()
                .orElse(null);
    }

    public String getRegion() {
        return region;
    }

    public String getDefaultCity() {
        return defaultCity;
    }
}
