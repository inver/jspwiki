/*
Class: Categories
    Turn wikipage links into AJAXed popups.

Depends:
    Wiki

DOM structure before:
    (start code)
    span.category
        a.wikipage Category-Page
    (end)

DOM structure after:
    (start code)
    div|span.category
        span
            a.wikipage.category-link[href=".."] Category-Page
            div.popup (.hidden|.loading|.active)
                div.title
                    a.wikipage[href=".."]Category-Page
                ul
                    li
                        a
                br
                a.morelink ..and x more
                br
    (end)
*/

Wiki.Category = function(element, pagename, xhrURL){

    var popup;
    'span.cage'.slick().wraps(element).grab(popup = 'div.popup.hide'.slick());

    element.set({
        'class': 'category-link',
        title: 'category.title'.localize( pagename ),
        events: { click: function(event){
            
            event.stop();
            popup.swapClass('hide', 'loading').onHover( /*popup.getParent()*/ );
            element.set('title','').removeEvents();

            new Request.HTML({
                url: xhrURL,
                data: { page:pagename },
                update: popup,
                onSuccess: function(){ popup.swapClass('loading', 'active').fade(0.9); }
            }).send();
            
        } }
    });

}
