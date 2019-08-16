

var columns = [
    [{
        field: "geneid",
        title: "GeneId",
        align: "center",
        halign: "center",
        sortable: true,
        yalign: "middle",
        formatter: function (value, row) {
            return "<a href='/US/SAAS/BGDB/genome/moreInfo?id=" + row.id + "' target='_blank' style='color: cornflowerblue;'>" + row.geneid + "</a>"
        }
    }, {
        field: "chr",
        title: "Chromesome",
        align: "center",
        halign: "center",
        yalign: "middle",
        sortable: true
    }, {
        field: "start",
        title: "Start",
        align: "center",
        halign: "center",
        yalign: "middle",
        sortable: true
    }, {
        field: "end",
        title: "End",
        align: "center",
        halign: "center",
        yalign: "middle",
        sortable: true
    }, {
        field: "strand",
        title: "Strand",
        align: "center",
        halign: "center",
        yalign: "middle",
        sortable: true
    }, {
        field: "pfam",
        title: "Pfam",
        align: "center",
        halign: "center",
        yalign: "middle",
        sortable: true,
        formatter: function (value, row) {
            if (row.pfam.indexOf("PF") == -1) {
                return row.pfam;
            } else {
                var pfamHtml = "";
                $.each(row.pfam.split(","), function (i, pfam) {
                    pfamHtml += "<p><a target='_blank' href='http://amigo.geneontology.org/amigo/term/'" + pfam.trim() + "'>" + pfam.trim() + "</a></p>";
                });
                return pfamHtml;
            }
        }
    }, {
        field: "panther",
        title: "Panther",
        align: "center",
        halign: "center",
        yalign: "middle",
        sortable: true
    }, {
        field: "kog",
        title: "KOG",
        align: "center",
        halign: "center",
        yalign: "middle",
        sortable: true
    }, {
        field: "kegg",
        title: "KEGG",
        align: "center",
        halign: "center",
        yalign: "middle",
        sortable: true,
        formatter: function (value, row) {
            if (row.kegg.indexOf(".") == -1) {
                return row.kegg;
            } else {
                var keggHtml = "";
                $.each(row.kegg.split(","), function (i, kegg) {
                    keggHtml += "<p><a target='_blank' href='https://www.kegg.jp/dbget-bin/www_bget?ec:'" + kegg.trim() + "'>" + kegg.trim() + "</a></p>";
                });
                return keggHtml;
            }
        }
    }, {
        field: "ko",
        title: "KO",
        align: "center",
        halign: "center",
        yalign: "middle",
        sortable: true,
        formatter: function (value, row) {
            return "<a target='_blank' href='https://www.kegg.jp/dbget-bin/www_bget?ko:" + row.ko + "'>" + row.ko + "</a>";
        }
    }, {
        field: "go",
        title: "GO",
        align: "center",
        halign: "center",
        yalign: "middle",
        sortable: true,
        formatter: function (value, row) {
            if (row.go.indexOf("GO") == -1) {
                return row.go;
            } else {
                var goHtml = "";
                $.each(row.go.split(","), function (i, go) {
                    goHtml += "<p><a target='_blank' href='http://amigo.geneontology.org/amigo/term/'" + go.trim() + "'>" + go.trim() + "</a></p>";
                });
                return goHtml;
            }
        }
    }, {
        field: "arabi_name",
        title: "Best-hit-arabi-name",
        align: "center",
        halign: "center",
        yalign: "middle",
        sortable: true,
        formatter: function (value, row) {
            return "<a href='https://apps.araport.org/thalemine/portal.do?externalids=" + row.arabi_name + "' target='_blank'>" + row.arabi_name +"</a>"
        }
    }, {
        field: "arabi_symbol",
        title: "Arabi-symbol",
        align: "center",
        halign: "center",
        yalign: "middle",
        sortable: true
    }, {
        field: "arabi_defline",
        title: "Arabi-defline",
        align: "center",
        halign: "center",
        yalign: "middle",
        sortable: true
    }]];

function createTable() {
    var array = ["GeneId", "Chromesome", "Start", "End", "Strand", "Pfam", "Panther", "KOG", "KEGG", "KO", "GO",
        "Best-hit-arabi-name", "Arabi-symbol", "Arabi-defline"];
    var values = ["geneid", "chr", "start", "end", "strand", "pfam", "panther", "kog", "kegg", "ko", "go", "arabi_name",
        "arabi_symbol", "arabi_defline"];
    var tHtml = "";

    var html = "";
    $.each(array, function (n, value) {
            html += "<label style='margin-right: 15px'>" +
                "<input type='checkbox' checked='checked' value='" + values[n] + "' onclick=\"setColumns('" + values[n] + "')\">" + value +
                "</label>"
        }
    );
    $("#checkbox").append(html);


}

function hideArray() {
    var hiddenArray = ["panther", "arabi_symbol", "arabi_defline"];

    $.each(hiddenArray, function (n, value) {
            $('#table').bootstrapTable('hideColumn', value);
            $("input:checkbox[value=" + value + "]").attr("checked", false)
        }
    );
}

function setColumns(value) {
    var element = $("input:checkbox[value=" + value + "]");

    if (element.is(":checked")) {
        $('#table').bootstrapTable('showColumn', value);
    } else {
        $('#table').bootstrapTable('hideColumn', value);
    }
}
