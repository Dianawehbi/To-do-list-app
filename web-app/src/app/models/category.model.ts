export interface CategoryResponseData {
  id: number;
  name: string;
  color: string; // hex code, e.g. "#3b82f6"
  active: boolean;
}

export class Category {
  public id: number;
  public name: string;
  public color: string;
  public active: boolean;

  constructor(data: {
    id: number;
    name: string;
    color: string;
    active: boolean;
  }) {
    this.id = data.id;
    this.name = data.name;
    this.color = data.color;
    this.active = data.active;
  }
}