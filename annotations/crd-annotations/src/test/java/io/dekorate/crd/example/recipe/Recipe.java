/**
 * Copyright 2018 The original authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
**/

package io.dekorate.crd.example.recipe;

import io.fabric8.kubernetes.api.model.Quantity;

public class Recipe {

  private String ingredient;

  private Quantity quantity;


  public String getIngredient() {
    return this.ingredient;
  }

  public void setIngredient(String ingredient) {
    this.ingredient=ingredient;
  }

  public Quantity getQuantity() {
    return this.quantity;
  }

  public void setQuantiy(Quantity quantity) {
    this.quantity=quantity;
  }

}
