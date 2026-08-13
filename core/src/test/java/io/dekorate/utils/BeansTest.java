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

package io.dekorate.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class BeansTest {

  private static class Person {
    private final String name;
    private final String address;
    private final String phone;

    public Person() {
      this(null, null, null);
    }

    public Person(String name, String address, String phone) {
      this.name = name;
      this.address = address;
      this.phone = phone;
    }

    public String getName() {
      return name;
    }

    public String getAddress() {
      return address;
    }

    public String getPhone() {
      return phone;
    }
  }

  private static class Team {
    private final String name;
    private final Person captain;
    private final List<Person> members;

    public Team() {
      this(null, null, null);
    }

    public Team(String name, Person captain, List<Person> members) {
      this.name = name;
      this.captain = captain;
      this.members = members;
    }

    public String getName() {
      return name;
    }

    public Person getCaptain() {
      return captain;
    }

    public List<Person> getMembers() {
      return members;
    }

  }

  @Test
  public void shouldCombineSimpleBeans() throws Exception {
    Person person1 = new Person("Jim", null, null);
    Person person2 = new Person(null, "Some place", null);
    Person combined = Beans.combine(person1, person2);
    assertNotNull(combined);
    assertEquals("Jim", combined.getName());
    assertEquals("Some place", combined.getAddress());
  }

  @Test
  public void shouldCombineSynteticObjects() throws Exception {
    Person captain = new Person("Jim", null, null);
    Person member1 = new Person("Joe", null, null);
    Team team1 = new Team("A", captain, null);
    List<Person> members = new ArrayList<>();
    members.add(member1);
    Team team2 = new Team("A", null, members);
    Team combined = Beans.combine(team1, team2);
    assertNotNull(combined);
    assertEquals("A", combined.getName());
    assertEquals(captain, combined.getCaptain());
    assertNotNull(combined.getMembers());
    assertEquals(member1, combined.getMembers().get(0));
  }

  @Test
  public void shouldCombineList() throws Exception {
    Person member1 = new Person("Joe", null, null);
    Person member2 = new Person("John", null, null);
    Person member3a = new Person("Jordan", "Some Place", null);
    Person member3b = new Person("Jordan", null, "555-12345");

    List<Person> team1Members = new ArrayList<>();
    team1Members.add(member1);
    team1Members.add(member2);
    team1Members.add(member3a);

    List<Person> team2Members = new ArrayList<>();
    team2Members.add(member3b);

    Team team1 = new Team("A", null, team1Members);
    Team team2 = new Team("A", null, team2Members);

    Team combined = Beans.combine(team1, team2);
    assertNotNull(combined);
    assertEquals("A", combined.getName());
    assertNotNull(combined.getMembers());
    assertEquals(3, combined.getMembers().size());

    Person combinePerson = combined.getMembers().stream().filter(p -> "Jordan".equals(p.name)).findFirst().get();
    assertNotNull(combinePerson);
    assertEquals("Some Place", combinePerson.getAddress());
    assertEquals("555-12345", combinePerson.getPhone());
  }

  @Test
  public void shouldCombineMaps() throws Exception {
    Person member1 = new Person("Joe", null, null);
    Person member2 = new Person("John", null, null);
    Person member3a = new Person("Jordan", "Some Place", null);
    Person member3b = new Person("Jordan", null, "555-12345");

    Map<String, Person> team1 = new HashMap<>();
    team1.put("Joe", member1);
    team1.put("John", member2);
    team1.put("Jordan", member3a);

    Map<String, Person> team2 = new HashMap<>();
    team2.put("Jordan", member3b);

    Map<String, Person> combined = Beans.combine(team1, team2);
    assertNotNull(combined);
    assertEquals(3, combined.size());

    Person combinePerson = combined.get("Jordan");
    assertNotNull(combinePerson);
    assertEquals("Some Place", combinePerson.getAddress());
    assertEquals("555-12345", combinePerson.getPhone());
  }
}
