package com.example.application.views.cardlist;

import java.util.ArrayList;
import java.util.List;

import com.example.application.models.Result;
import com.example.application.service.BookService;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.example.application.views.main.MainView;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.component.dependency.CssImport;
import elemental.json.JsonObject;


@Route(value = "card-list", layout = MainView.class)
@RouteAlias(value = "", layout = MainView.class)
@PageTitle("Card List")
@CssImport("./views/cardlist/card-list-view.css")
public class CardListView extends Div implements AfterNavigationObserver {


    private BookService bookService;
    private Grid<Result> grid = new Grid<>();
    private int startIndex = 0;
    private boolean isLoading = false;

    private List<Result> items = new ArrayList<>();
    private TextField textField;
    private String searchTerm;

    public CardListView(BookService bookService) {
        this.bookService = bookService;
        textField = new TextField();
        textField.setLabel("Search Term");
        textField.setPlaceholder("search...");
        textField.setAutofocus(true);
        textField.setWidthFull();
        textField.addKeyDownListener(keyDownEvent -> {
                    String keyStroke = keyDownEvent.getKey().getKeys().toString();
                    if (keyStroke.equals("[Enter]") && !isLoading) {
                        System.out.println(textField.getValue());
                        searchTerm = textField.getValue();
                        startIndex = 0;
                        items.clear();
                        getBooks(searchTerm);
                    }
                }
        );
        addClassName("card-list-view");
        setSizeFull();
        grid.setHeight("100%");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS);
        grid.addComponentColumn(Result -> createCard(Result));
        grid.addItemClickListener(new ComponentEventListener<ItemClickEvent<Result>>() {
            @Override
            public void onComponentEvent(ItemClickEvent<Result> itemItemClickEvent) {
                System.out.println(itemItemClickEvent.getItem());
            }
        });

        add(textField, withClientsideScrollListener(grid));
    }

    private void getBooks(String searchTerm) {
        isLoading = true;

        bookService.getBooks(volumesResponse -> {
            //we must fire this callback on the UI thread and that is why we use getUi().get().acccess(() ->
            getUI().get().access(() -> {

                //this is the callback result, so volumesResponse is the volumesResponse returned from
                //      void operationFinished(T results);
                items.addAll(volumesResponse.getResults());
                grid.setItems(items);
                startIndex += BookService.MAX_RESULTS;
                isLoading = false;
                //https://vaadin.com/docs/v14/flow/advanced/tutorial-push-access
                //we need to notify the browser when we are done. Note that the parent-view MainView is annotated with
                //the @Push annotation, which will force the views to refresh themselves, including the grid.
                getUI().get().push();

            });
        }, searchTerm, BookService.MAX_RESULTS, startIndex);

    }


    private Grid<Result> withClientsideScrollListener(Grid<Result> grid) {
        grid.getElement().executeJs(
                "this.$.scroller.addEventListener('scroll', (scrollEvent) => " +
                        "{requestAnimationFrame(" +
                        "() => $0.$server.onGridScroll({sh: this.$.table.scrollHeight, " +
                        "ch: this.$.table.clientHeight, " +
                        "st: this.$.table.scrollTop}))},true)",
                getElement());
        return grid;
    }

    @ClientCallable
    public void onGridScroll(JsonObject scrollEvent) {
        int scrollHeight = (int) scrollEvent.getNumber("sh");
        int clientHeight = (int) scrollEvent.getNumber("ch");
        int scrollTop = (int) scrollEvent.getNumber("st");

        double percentage = (double) scrollTop / (scrollHeight - clientHeight);
        //reached the absolute bottom of the scroll
        if (percentage == 1.0) {

            if (!isLoading) {
                getBooks(searchTerm);
            }
            grid.scrollToIndex(items.size() / 2);

        }

    }

    private HorizontalLayout createCard(Result Result) {
        HorizontalLayout card = new HorizontalLayout();
        card.addClassName("card");
        card.setSpacing(false);
        card.getThemeList().add("spacing-s");

        Image image = new Image();
        image.setSrc(getSmallThumbnail(Result));
        VerticalLayout description = new VerticalLayout();
        description.addClassName("description");
        description.setSpacing(false);
        description.setPadding(false);

        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("header");
        header.setSpacing(false);
        header.getThemeList().add("spacing-s");

        Span name = new Span(getAuthor(Result));
        name.addClassName("name");
        Span date = new Span(getPublisher(Result));
        date.addClassName("date");
        header.add(name, date);


        description.add(header);
        card.add(image, description);
        return card;
    }

    private String getPublisher(Result Result) {
        if (null == Result.getOpeningDate()) {
            return "";
        }
        return Result.getPublicationDate();
    }

    private String getAuthor(Result Result) {
        if (null == Result.getDisplayTitle()) {
            return "";
        }
        return Result.getDisplayTitle();
    }

    private String getSmallThumbnail(Result Result) {
        return "https://randomuser.me/api/portraits/men/16.jpg";
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        // we don't need to do anything here now. Possibly remove interface and this contract-method.
    }


}
