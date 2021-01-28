- в этой директории находятся кастомизированные lua-скрипты, написанные специально
для обеспечения BGP-функциональности стэка SSO.
- файлы:
    - healthcheck.lua - собственно сам модифицированный скрипт из комплекта openresty с куском кода, который управляет
    BGP-пирингом.
    - lua-healthcheck.conf - просто include c location {}, подключающей функциональность openresty и обеспечивающей 
    авторизацию по IP.
    - lua.conf - скрипт, описывающий, что именно модуль resty.upstream.healthcheck должен быть подключен и содержащий 
    его конфигурацию, в частности URI проверки статуса сервера приложений - /auth/realms/master/status/health. Этот 
    URI в свою очередь обрабатывается контейнером SSO запущенным внутри WildFly.

Стандартный модуль resty.upstream.healthcheck был кастомизирован, чтобы обеспечить возможность управления BGP-транками
в случае аварии на сегменте SSO. В стандартную функцию do_check() был добавлен дополнительный фрагмент кода ниже:

===Cut===
local peernum = #ctx.primary_peers
    -- this is the thing
    local atleastonepeerisup = false
    errlog("healthcheck: starting new upstream check")
    if peernum == 0 then
        return
    else
        local i = 1
        while (i <= peernum) and not atleastonepeerisup do
            local peer = ctx.primary_peers[i]
            if not peer.down then
                errlog("healthcheck: found one peer that's UP:"..peer.name..", bailing out")
                atleastonepeerisup = true
                handle_bird("enable ibgpv4dcr1")
                handle_bird("enable ibgpv4dcr2")
            else
                errlog("healthcheck: oops, "..peer.name.." is DOWN")
            end
            i = i + 1
        end
        if not atleastonepeerisup then
            -- now we need to shut down the BGP link
            errlog("healthcheck: all peers are DOWN, shutting down the BGP session")
            handle_bird("disable ibgpv4dcr1")
            handle_bird("disable ibgpv4dcr2")
        end
    end
===Cut===

Так как код простой, сомнительно, чтобы были нужны какие-то дополнительные комментацрии кроме нижеследующего: если хотя бы 
один сервер приложений не ответил, то никаких действий не предпринимать, если не ответили все - приложить оба BGP-транка 
вызовом нижеследующей процедуры и отправкой ей команды в терминах bird.

===Cut===
local function handle_bird(spell)
    local path = "/var/run/bird/bird.ctl"
    local socket = require("socket")
    socket.unix = require("socket.unix")

    local unix = assert(socket.unix())

    local status, errstr = unix:connect(path);
    if status ~= nil then
        status, errstr = unix:send(spell.."\r\n");

        if status ~= nil then
            completed = false
            while not completed do
                local s = unix:receive()
                print(s)
                local strlength = string.len(s)
                local strstart, strend = string.find(s,"0000")
                if strstart == 1 and strend == 4 and strlength == 5 then
                    completed = true
                end
            end
        else
            errlog("cannot send to bird socket")
        end
        unix:close()
    else
        errlog("cannot connect to bird socket")
    end
end
===Cut===