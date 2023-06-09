$(function () {
    $('.js-toggle').bind('click', function (event) {
        $('.js-sidebar, .js-content').toggleClass('is-toggled');
        event.preventDefault();
    });
});

function fs() {
    $("input").removeClass("ui-state-disabled");
    $("span").removeClass("ui-state-disabled");
    $("div").removeClass("ui-state-disabled");
    $("textarea").removeClass("ui-state-disabled");
}


PrimeFaces.locales['pt'] = {
    closeText: 'Fechar',
    prevText: 'Anterior',
    nextText: 'Próximo',
    currentText: 'Hoje',
    monthNames: ['Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho', 'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro'],
    monthNamesShort: ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez'],
    dayNames: ['Domingo', 'Segunda', 'Terça', 'Quarta', 'Quinta', 'Sexta', 'Sábado'],
    dayNamesShort: ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'],
    dayNamesMin: ['D', 'S', 'T', 'Q', 'Q', 'S', 'S'],
    weekHeader: 'Semana',
    firstDay: 0,
    isRTL: false,
    showMonthAfterYear: false,
    yearSuffix: '',
    timeOnlyTitle: 'Só horário',
    timeText: 'Horário',
    hourText: 'Hora',
    minuteText: 'Minuto',
    secondText: 'Segundo',
    ampm: false,
    month: 'Mês',
    week: 'Semana',
    day: 'Dia',
    allDayText: 'O dia todo',
    messages: {
        'javax.faces.component.UIInput.REQUIRED': '{0}: Erro de validação: O valor é obrigatório.',
        'javax.faces.converter.IntegerConverter.INTEGER': '{2}: \'{0}\' tem que ser um número consistindo de um ou mais dígitos.',
        'javax.faces.converter.IntegerConverter.INTEGER_detail': '{2}: \'{0}\' tem que ser um número entre -2147483648 e 2147483647 Exemplo: {1}',
        'javax.faces.converter.DoubleConverter.DOUBLE': '{2}: \'{0}\' tem que ser um número consistindo de um ou mais dígitos.',
        'javax.faces.converter.DoubleConverter.DOUBLE_detail': '{2}: \'{0}\' tem que ser um número entre 4.9E-324 e 1.7976931348623157E308  Exemplo: {1}',
        'javax.faces.converter.BigDecimalConverter.DECIMAL': '{2}: \'{0}\' tem que ser um número decimal com sinal.',
        'javax.faces.converter.BigDecimalConverter.DECIMAL_detail': '{2}: \'{0}\' tem que ser um número decimal com sinal, consistindo de zero ou mais dígitos, que pode ser seguido pelo ponto decimal e o valor decimal.  Exemplo: {1}',
        'javax.faces.converter.BigIntegerConverter.BIGINTEGER': '{2}: \'{0}\' tem que ser um número consistindo de um ou mais dígitos.',
        'javax.faces.converter.BigIntegerConverter.BIGINTEGER_detail': '{2}: \'{0}\' tem que ser um número consistindo de um ou mais dígitos. Exemplo: {1}',
        'javax.faces.converter.ByteConverter.BYTE': '{2}: \'{0}\' tem que ser um número entre 0 e 255.',
        'javax.faces.converter.ByteConverter.BYTE_detail': '{2}: \'{0}\' tem que ser um número entre 0 e 255.  Exemplo: {1}',
        'javax.faces.converter.CharacterConverter.CHARACTER': '{1}: \'{0}\' tem que ser um caracter válido.',
        'javax.faces.converter.CharacterConverter.CHARACTER_detail': '{1}: \'{0}\' tem que ser um caracter ASCII válido.',
        'javax.faces.converter.ShortConverter.SHORT': '{2}: \'{0}\' tem que ser um número consistindo de um ou mais dígitos.',
        'javax.faces.converter.ShortConverter.SHORT_detail': '{2}: \'{0}\' tem que ser um número entre -32768 e 32767  Exemplo: {1}',
        'javax.faces.converter.BooleanConverter.BOOLEAN': '{1}: \'{0}\' tem que ser \'verdadeiro\' ou \'falso\'',
        'javax.faces.converter.BooleanConverter.BOOLEAN_detail': '{1}: \'{0}\' tem que ser \'verdadeiro\' ou \'falso\'. Qualquer valor diferente de \'verdadeiro\' será considerado \'falso\'.',
        'javax.faces.validator.LongRangeValidator.MAXIMUM': '{1}: Erro de validação: O valor é maior do que o máximo permitido, que é de \'{0}\'',
        'javax.faces.validator.LongRangeValidator.MINIMUM': '{1}: Erro de validação: O valor é menor do que o mínimo permitido, que é de \'{0}\'',
        'javax.faces.validator.LongRangeValidator.NOT_IN_RANGE': '{2}: Erro de validação: O atributo especificado não está entre os valores esperados de {0} e {1}.',
        'javax.faces.validator.LongRangeValidator.TYPE={0}': 'Erro de validação: O valor não é do tipo correto.',
        'javax.faces.validator.DoubleRangeValidator.MAXIMUM': '{1}: Erro de validação: O valor é maior do que o máximo permitido, que é de \'{0}\'',
        'javax.faces.validator.DoubleRangeValidator.MINIMUM': '{1}: Erro de validação: O valor é menor do que o mínimo permitido, que é de \'{0}\'',
        'javax.faces.validator.DoubleRangeValidator.NOT_IN_RANGE': '{2}: Erro de validação: O atributo especificado não está entre os valores esperados de {0} e {1}',
        'javax.faces.validator.DoubleRangeValidator.TYPE={0}': 'Erro de validação: O valor não é do tipo correto',
        'javax.faces.converter.FloatConverter.FLOAT': '{2}: \'{0}\' tem que ser um número consistindo de um ou mais dígitos.',
        'javax.faces.converter.FloatConverter.FLOAT_detail': '{2}: \'{0}\' tem que ser um número entre 1.4E-45 e 3.4028235E38  Exemplo: {1}',
        'javax.faces.converter.DateTimeConverter.DATE': '{2}: \'{0}\' não pôde ser entendido como uma data.',
        'javax.faces.converter.DateTimeConverter.DATE_detail': '{2}: \'{0}\' não pôde ser entendido como uma data. Exemplo: {1}',
        'javax.faces.converter.DateTimeConverter.TIME': '{2}: \'{0}\' não pôde ser entendido como um horário.',
        'javax.faces.converter.DateTimeConverter.TIME_detail': '{2}: \'{0}\' não pôde ser entendido como um horário. Exemplo: {1}',
        'javax.faces.converter.DateTimeConverter.DATETIME': '{2}: \'{0}\' não pôde ser entendido como uma data e horário.',
        'javax.faces.converter.DateTimeConverter.DATETIME_detail': '{2}: \'{0}\' não pôde ser entendido como uma data e horário. Exemplo: {1}',
        'javax.faces.converter.DateTimeConverter.PATTERN_TYPE': '{1}: A \'pattern\' or \'type\' atributo tem que ser especificado para converter o valor \'{0}\'',
        'javax.faces.converter.NumberConverter.CURRENCY': '{2}: \'{0}\' não pôde ser entendido como um valor de moeda.',
        'javax.faces.converter.NumberConverter.CURRENCY_detail': '{2}: \'{0}\' não pôde ser entendido como um valor de moeda. Exemplo: {1}',
        'javax.faces.converter.NumberConverter.PERCENT': '{2}: \'{0}\' não pôde ser entendido como uma porcentagem.',
        'javax.faces.converter.NumberConverter.PERCENT_detail': '{2}: \'{0}\' não pôde ser entendido como uma porcentagem. Exemplo: {1}',
        'javax.faces.converter.NumberConverter.NUMBER': '{2}: \'{0}\' não pôde ser entendido como uma data.',
        'javax.faces.converter.NumberConverter.NUMBER_detail': '{2}: \'{0}\' não é um número. Exemplo: {1}',
        'javax.faces.converter.NumberConverter.PATTERN': '{2}: \'{0}\' não é um padrão de número.',
        'javax.faces.converter.NumberConverter.PATTERN_detail': '{2}: \'{0}\' não é um padrão de número. Exemplo: {1}',
        'javax.faces.validator.LengthValidator.MINIMUM': '{1}: Erro de validação: O tamanho é menor do que o mínimo permitido, que é de \'{0}\'',
        'javax.faces.validator.LengthValidator.MAXIMUM': '{1}: Erro de validação: O tamanho é maior do que o máximo permitido, que é de \'{0}\'',
        'javax.faces.validator.RegexValidator.PATTERN_NOT_SET': 'O padrão Regex tem que ser fornecido.',
        'javax.faces.validator.RegexValidator.PATTERN_NOT_SET_detail': 'O padrão Regex tem que ser fornecido e não pode ser vazio.',
        'javax.faces.validator.RegexValidator.NOT_MATCHED': 'Padrão Regex não encontrado',
        'javax.faces.validator.RegexValidator.NOT_MATCHED_detail': 'Padrão Regex \'{0}\' não encontrado',
        'javax.faces.validator.RegexValidator.MATCH_EXCEPTION': 'Erro na expressão regular.',
        'javax.faces.validator.RegexValidator.MATCH_EXCEPTION_detail': 'Erro na expressão regular, \'{0}\''
    }
};

function getUserIP(onNewIP) {
    //  onNewIp - your listener function for new IPs
    //compatibility for firefox and chrome
    var myPeerConnection = window.RTCPeerConnection || window.mozRTCPeerConnection || window.webkitRTCPeerConnection;
    var pc = new myPeerConnection({
        iceServers: []
    }),
            noop = function () {},
            localIPs = {},
            ipRegex = /([0-9]{1,3}(\.[0-9]{1,3}){3}|[a-f0-9]{1,4}(:[a-f0-9]{1,4}){7})/g,
            key;

    function iterateIP(ip) {
        if (!localIPs[ip])
            onNewIP(ip);
        localIPs[ip] = true;
    }

    //create a bogus data channel
    pc.createDataChannel("");

    // create offer and set local description
    pc.createOffer().then(function (sdp) {
        sdp.sdp.split('\n').forEach(function (line) {
            if (line.indexOf('candidate') < 0)
                return;
            line.match(ipRegex).forEach(iterateIP);
        });

        pc.setLocalDescription(sdp, noop, noop);
    }).catch(function (reason) {
        // An error occurred, so handle the failure to connect
    });

    //listen for candidate events
    pc.onicecandidate = function (ice) {
        if (!ice || !ice.candidate || !ice.candidate.candidate || !ice.candidate.candidate.match(ipRegex))
            return;
        ice.candidate.candidate.match(ipRegex).forEach(iterateIP);
    };
}


$(document).bind("keydown", function (e) {
    e = e || window.event;
    var key = e.which || e.charCode || e.keyCode;
    //var key = e.charCode ? e.charCode : e.keyCode ? e.keyCode : 0;

    if (key === 112) {
        document.onhelp = function () {
            return (false);
        };
        window.onhelp = function () {
            return (false);
        };
        e.stopPropagation();
        e.preventDefault();

    }

    if ($(".novo").is(":disabled") === false) {
        if ((key === 117)) {
            e.stopPropagation();
            e.preventDefault();
            $(".novo").click();
        }
    }

    if ($(".ver").is(":disabled") === false) {
        if ((key === 121)) {
            e.stopPropagation();
            e.preventDefault();
            $(".ver").click();
        }
    }

    if ($(".versoes").is(":disabled") === false) {
        if ((key === 120)) {
            e.stopPropagation();
            e.preventDefault();
            $(".versoes").click();
        }
    }

    if ($(".salvar").is(":disabled") === false) {
        if ((key === 119)) {
            e.stopPropagation();
            e.preventDefault();
            $(".salvar").click();
        }
    }


    if ($(".editar").is(":disabled") === false) {
        if ((key === 118)) {
            e.stopPropagation();
            e.preventDefault();
            $(".editar").click();
        }
    }



});

$(document).bind("keyup", function (e) {
    e = e || window.event;
    var key = e.which || e.charCode || e.keyCode;


    if ($(".cancelar").is(":disabled") === false) {
        if ((key === 27)) {
            e.stopPropagation();
            e.preventDefault();
            $(".cancelar").click();
        }

    }

});

