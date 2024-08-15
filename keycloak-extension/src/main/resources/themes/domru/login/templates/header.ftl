<#macro defaultTemplate withCity=true >
  <header id="page-header" class="flex items-center">
    <div class="w-full flex justify-between">
      <a href="${(homePage)!"https://newlkb2b.dom.ru"}">
        <div class="h-30px w-200px bg-contain bg-no-repeat logo logo--domru"></div>
      </a>
      <div class="flex items-center xl:items-start">
        <div id="cities-button" class=${withCity ? string("sm:mr-10", "sm:mr-10")} data-city=""></div>

        <a href="${(phoneConstLink)!"tel:88005500479"}"
           class= ${withCity ? string("phone--hidden-small phone-call-center", "phone--hidden-small phone-call-center")}>
          <div class="flex h-6 items-center">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path fill-rule="evenodd" clip-rule="evenodd" d="M7.4105 12.5728C9.667 14.8394 11.5778 15.9264 12.8877 16.449C13.8701 16.8409 14.9753 16.414 15.8002 15.4399L16.2176 14.9471C16.6723 14.4101 16.6403 13.6123 16.144 13.1137L15.3613 12.3275C14.9962 11.9608 14.4545 11.8359 13.9668 12.0061L13.6121 12.1298C12.2935 12.5899 10.829 12.2524 9.84196 11.2609L8.71652 10.1305C7.72945 9.13899 7.39344 7.66802 7.85147 6.34348L7.97468 5.9872C8.14409 5.4973 8.01981 4.95324 7.65473 4.58653L6.87196 3.80026C6.37564 3.30174 5.5814 3.26958 5.04676 3.72638L4.55609 4.1456C3.58633 4.97416 3.16137 6.08432 3.55153 7.07109C4.07179 8.38689 5.15399 10.3062 7.4105 12.5728ZM12.409 17.6599C10.9223 17.0668 8.86491 15.8763 6.49331 13.4941C4.12171 11.1119 2.93648 9.04535 2.34603 7.552C1.67657 5.85886 2.49878 4.19289 3.71569 3.15317L4.20635 2.73395C5.25583 1.83727 6.8149 1.90039 7.78914 2.87898L8.57192 3.66525C9.28856 4.38509 9.53251 5.45306 9.19997 6.4147L9.07676 6.77099C8.78186 7.62377 8.9982 8.57083 9.63371 9.20918L10.7592 10.3397C11.3947 10.978 12.3375 11.1953 13.1865 10.8991L13.5412 10.7753C14.4986 10.4413 15.5618 10.6863 16.2784 11.4062L17.0612 12.1925C18.0355 13.171 18.0983 14.7371 17.2056 15.7912L16.7882 16.2841C15.7531 17.5064 14.0946 18.3323 12.409 17.6599Z" fill="#222222"/>
            </svg>
            <span class="phone-number">${(phoneConst)!"8 800 333 9000"}</span>
          </div>
        </a>
      </div>
    </div>
  </header>
</#macro>
