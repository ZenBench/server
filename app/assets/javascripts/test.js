$(function(){
    $(document).ready(function() {
        $('.component-item').on('click', function(){
            getRuns(runsByComponent.bind(null, $(this).attr('data-ref'), $('#test').attr('data-id')));
        })
    })

    function runsByComponent(ref, id, data){
        var filtered = data.filter(function(run){
            return run.env.some(function(element){
                return element.ref === ref;
            }) && run.metrics.some(function(element){
                return element.id === id;
            });
        });
        alert(JSON.stringify(filtered));
        getRuns(runEnv.bind(null, filtered[0].id));
    }
    
    function runEnv(runId, data){
        var filtered = data.filter(function(run){
            return run.id === runId;
        });
        alert(JSON.stringify(filtered));
    }

    function getRuns(callback){
        $.getJSON("/runs", callback);
    }
})
