$(function(){
    $(document).ready(function() {
        $('.component-item').on('click', function(){
            getRuns(runsByComponent.bind(null, $(this).attr('data-ref'), $('#test').attr('data-id')));
        })
    })

    function runsByComponent(ref, id, data){
        var tmp_div,
            fragment = document.createDocumentFragment(),
            filtered = data.filter(function(run){
            return run.env.some(function(element){
                return element.ref === ref;
            }) && run.metrics.some(function(element){
                return element.id === id;
            });
        });
                
/*        filtered.map(function(element){
            tmp_div = document.createElement('div');
            tmp_div.id = element.id;
            tmp_div.innerHtml = $('#results ul li').html();
            tmp_div.textContent = element.id;
            $(tmp_div).on('click', getRuns(runEnv.bind(null, filtered[0].id)));
            fragment.appendChild(tmp_div);
        });*/

    }
    
    function runEnv(runId, data){
        var filtered = data.filter(function(run){
            return run.id === runId;
        });
    }

    function getRuns(callback){
        $.getJSON("/runs", callback);
    }
})
