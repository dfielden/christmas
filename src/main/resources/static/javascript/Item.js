export class Item {
    product;
    price;
    location;
    url;
    additionalInfo;
    selected;
    selectedBy

    constructor(product, price, location, url, additionalInfo, selected, selectedBy) {
        this.product = product;
        this.price = price;
        this.location = location;
        this.url = url;
        this.additionalInfo = additionalInfo;
        this.selected = selected;
        this.selectedBy = selectedBy;
    }
}
