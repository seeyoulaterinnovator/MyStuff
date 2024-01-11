<#macro defaultTemplate withCity=true>
  <header id="page-header" class="flex items-center">
    <div class="w-full flex justify-between">
      <a href="https://lkb2b.tcenter.ru">
        <div class="h-30px w-120px md:h-10 md:w-40 xl:h-16 xl:w-64 bg-contain bg-no-repeat logo logo--base"></div>
      </a>
      <div class="flex items-center xl:items-start">
        <div id="cities-button" class=${withCity ? string("sm:mr-10", "sm:mr-10")} data-city=""></div>

        <a href="tel:84951459555"
           class= ${withCity ? string("phone--hidden-small phone-call-center", "phone--hidden-small phone-call-center")}>
          <div class="flex h-6 items-center">
            <svg width="15" height="14" viewBox="0 0 15 14" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M3.03341 1.1759C3.2853 0.924002 3.6937 0.924002 3.9456 1.1759L5.99182 3.22212C6.38346 3.61376 6.38346 4.24873 5.99182 4.64037C5.48302 5.14918 5.28745 5.89309 5.47635 6.58572C5.89974 8.13816 7.11969 9.35811 8.67213 9.7815C9.36476 9.9704 10.1087 9.77483 10.6175 9.26603C11.0091 8.87439 11.6441 8.87439 12.0357 9.26603L13.3419 10.5722C13.6933 10.9236 13.6933 11.4935 13.3419 11.8449C12.4689 12.7179 11.4536 13.2821 10.4491 13.4476C9.45708 13.6111 8.45158 13.3916 7.54609 12.6543C6.8175 12.0611 5.95941 11.292 4.96264 10.2952C3.83397 9.16654 2.95756 8.17608 2.27924 7.33067C0.787387 5.47133 1.23894 2.97036 3.03341 1.1759Z"
                    stroke="black"/>
            </svg>
            <span class="phone-number">8 (495) 145 9 555</span>
          </div>
        </a>
      </div>
    </div>
  </header>
</#macro>
