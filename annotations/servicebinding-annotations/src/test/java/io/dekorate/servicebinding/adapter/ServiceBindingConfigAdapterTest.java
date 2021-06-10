package io.dekorate.servicebinding.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.dekorate.servicebinding.config.ServiceBindingConfig;

public class ServiceBindingConfigAdapterTest {

  @Test
  public void testServiceBinding() {

    final HashMap<String, Object> services[] = new HashMap[1];
    services[0] = new HashMap<String, Object>() {
      {
        put("group", "postgresql.dev");
        put("kind", "Database");
        put("name", "demo-database");
        put("id", "postgresDB");

      }
    };

    final HashMap<String, Object> application = new HashMap<String, Object>();
    application.put("group", "apps");
    application.put("resource", "Deployment");
    application.put("version", "v1alpha1");
    application.put("name", "servicebinding-test");

    Map<String, Object> map = new HashMap<String, Object>() {
      {
        put("name", "servicebinding-test-binding");
        put("application", application);
        put("services", services);
        put("envVarPrefix", "postgres");
      }
    };

    ServiceBindingConfig config = ServiceBindingConfigAdapter.adapt(map);

    assertNotNull(config);

    assertEquals("apps", config.getApplication().getGroup());
    assertEquals("Deployment", config.getApplication().getResource());
    assertEquals("v1alpha1", config.getApplication().getVersion());
    assertEquals("servicebinding-test", config.getApplication().getName());

    assertEquals(1, config.getServices().length);
    assertEquals("postgresql.dev", config.getServices()[0].getGroup());
    assertEquals("Database", config.getServices()[0].getKind());
    assertEquals("demo-database", config.getServices()[0].getName());
    assertEquals("postgresDB", config.getServices()[0].getId());

    assertEquals("postgres", config.getEnvVarPrefix());
  }

  @Test
  public void testServiceBindingWithBindingPath() {

    final HashMap<String, Object> services[] = new HashMap[1];
    services[0] = new HashMap<String, Object>() {
      {
        put("group", "postgresql.dev");
        put("kind", "Database");
        put("name", "demo-database");
        put("id", "postgresDB");
      }
    };

    final HashMap<String, Object> application = new HashMap<String, Object>();
    application.put("group", "apps");
    application.put("resource", "Deployment");
    application.put("version", "v1alpha1");
    application.put("name", "servicebinding-test");

    final HashMap<String, String> bindingPath = new HashMap<String, String>();
    bindingPath.put("secretPath", "/var");
    bindingPath.put("containerPath", ".spec");

    Map<String, Object> map = new HashMap<String, Object>() {
      {
        put("name", "servicebinding-test-binding");
        put("application", application);
        put("services", services);
        put("bindingPath", bindingPath);
      }
    };

    ServiceBindingConfig config = ServiceBindingConfigAdapter.adapt(map);

    assertNotNull(config);

    assertEquals("apps", config.getApplication().getGroup());
    assertEquals("Deployment", config.getApplication().getResource());
    assertEquals("v1alpha1", config.getApplication().getVersion());
    assertEquals("servicebinding-test", config.getApplication().getName());

    assertEquals(1, config.getServices().length);
    assertEquals("postgresql.dev", config.getServices()[0].getGroup());
    assertEquals("Database", config.getServices()[0].getKind());
    assertEquals("demo-database", config.getServices()[0].getName());
    assertEquals("postgresDB", config.getServices()[0].getId());

    assertEquals("/var", config.getBindingPath().getSecretPath());
    assertEquals(".spec", config.getBindingPath().getContainerPath());
  }

  @Test
  public void testServiceBindingWithCustomEnvVar() {

    final HashMap<String, Object> services[] = new HashMap[1];
    services[0] = new HashMap<String, Object>() {
      {
        put("group", "postgresql.dev");
        put("kind", "Database");
        put("name", "demo-database");
        put("id", "postgresDB");
      }
    };

    final HashMap<String, Object> customEnvVar[] = new HashMap[1];
    customEnvVar[0] = new HashMap<String, Object>() {
      {
        put("name", "foo");
        put("value", "bar");
      }
    };

    final HashMap<String, Object> application = new HashMap<String, Object>();
    application.put("group", "apps");
    application.put("resource", "Deployment");
    application.put("version", "v1alpha1");
    application.put("name", "servicebinding-test");

    Map<String, Object> map = new HashMap<String, Object>() {
      {
        put("name", "servicebinding-test-binding");
        put("application", application);
        put("services", services);
        put("envVarPrefix", "postgres");
        put("customEnvVar", customEnvVar);
      }
    };

    ServiceBindingConfig config = ServiceBindingConfigAdapter.adapt(map);

    assertNotNull(config);

    assertEquals("apps", config.getApplication().getGroup());
    assertEquals("Deployment", config.getApplication().getResource());
    assertEquals("v1alpha1", config.getApplication().getVersion());
    assertEquals("servicebinding-test", config.getApplication().getName());

    assertEquals(1, config.getServices().length);
    assertEquals("postgresql.dev", config.getServices()[0].getGroup());
    assertEquals("Database", config.getServices()[0].getKind());
    assertEquals("demo-database", config.getServices()[0].getName());
    assertEquals("postgresDB", config.getServices()[0].getId());

    assertEquals("foo", config.getCustomEnvVar()[0].getName());
    assertEquals("bar", config.getCustomEnvVar()[0].getValue());
  }

  @Test
  public void testServiceBindingWithCustomEnvVarWithValueFromConfigMapKeyRef() {

    final HashMap<String, Object> services[] = new HashMap[1];
    services[0] = new HashMap<String, Object>() {
      {
        put("group", "postgresql.dev");
        put("kind", "Database");
        put("name", "demo-database");
        put("id", "postgresDB");
      }
    };

    final HashMap<String, Object> customEnvVar[] = new HashMap[1];
    customEnvVar[0] = new HashMap<String, Object>() {
      {
        put("name", "foo");
        put("value", "bar");
        put("configmap", "baz");
      }
    };

    final HashMap<String, Object> application = new HashMap<String, Object>();
    application.put("group", "apps");
    application.put("resource", "Deployment");
    application.put("version", "v1alpha1");
    application.put("name", "servicebinding-test");

    Map<String, Object> map = new HashMap<String, Object>() {
      {
        put("name", "servicebinding-test-binding");
        put("application", application);
        put("services", services);
        put("envVarPrefix", "postgres");
        put("customEnvVar", customEnvVar);
      }
    };

    ServiceBindingConfig config = ServiceBindingConfigAdapter.adapt(map);

    assertNotNull(config);

    assertEquals("apps", config.getApplication().getGroup());
    assertEquals("Deployment", config.getApplication().getResource());
    assertEquals("v1alpha1", config.getApplication().getVersion());
    assertEquals("servicebinding-test", config.getApplication().getName());

    assertEquals(1, config.getServices().length);
    assertEquals("postgresql.dev", config.getServices()[0].getGroup());
    assertEquals("Database", config.getServices()[0].getKind());
    assertEquals("demo-database", config.getServices()[0].getName());
    assertEquals("postgresDB", config.getServices()[0].getId());

    assertEquals("foo", config.getCustomEnvVar()[0].getName());
    assertEquals("bar", config.getCustomEnvVar()[0].getValue());
    assertEquals("baz", config.getCustomEnvVar()[0].getConfigmap());
  }

  @Test
  public void testServiceBindingWithCustomEnvVarWithValueFromSecretKeyRef() {

    final HashMap<String, Object> services[] = new HashMap[1];
    services[0] = new HashMap<String, Object>() {
      {
        put("group", "postgresql.dev");
        put("kind", "Database");
        put("name", "demo-database");
        put("id", "postgresDB");
      }
    };

    final HashMap<String, Object> customEnvVar[] = new HashMap[1];
    customEnvVar[0] = new HashMap<String, Object>() {
      {
        put("name", "foo");
        put("value", "bar");
        put("secret", "baz");
      }
    };

    final HashMap<String, Object> application = new HashMap<String, Object>();
    application.put("group", "apps");
    application.put("resource", "Deployment");
    application.put("version", "v1alpha1");
    application.put("name", "servicebinding-test");

    Map<String, Object> map = new HashMap<String, Object>() {
      {
        put("name", "servicebinding-test-binding");
        put("application", application);
        put("services", services);
        put("envVarPrefix", "postgres");
        put("customEnvVar", customEnvVar);
      }
    };

    ServiceBindingConfig config = ServiceBindingConfigAdapter.adapt(map);

    assertNotNull(config);

    assertEquals("apps", config.getApplication().getGroup());
    assertEquals("Deployment", config.getApplication().getResource());
    assertEquals("v1alpha1", config.getApplication().getVersion());
    assertEquals("servicebinding-test", config.getApplication().getName());

    assertEquals(1, config.getServices().length);
    assertEquals("postgresql.dev", config.getServices()[0].getGroup());
    assertEquals("Database", config.getServices()[0].getKind());
    assertEquals("demo-database", config.getServices()[0].getName());
    assertEquals("postgresDB", config.getServices()[0].getId());

    assertEquals("foo", config.getCustomEnvVar()[0].getName());
    assertEquals("bar", config.getCustomEnvVar()[0].getValue());
    assertEquals("baz", config.getCustomEnvVar()[0].getSecret());
  }

  @Test
  public void testServiceBindingWithCustomEnvVarWithValueFromFieldRef() {

    final HashMap<String, Object> services[] = new HashMap[1];
    services[0] = new HashMap<String, Object>() {
      {
        put("group", "postgresql.dev");
        put("kind", "Database");
        put("name", "demo-database");
        put("id", "postgresDB");
      }
    };

    final HashMap<String, Object> customEnvVar[] = new HashMap[1];
    customEnvVar[0] = new HashMap<String, Object>() {
      {
        put("name", "foo");
        put("value", "bar");
        put("field", "baz");
      }
    };

    final HashMap<String, Object> application = new HashMap<String, Object>();
    application.put("group", "apps");
    application.put("resource", "Deployment");
    application.put("version", "v1alpha1");
    application.put("name", "servicebinding-test");

    Map<String, Object> map = new HashMap<String, Object>() {
      {
        put("name", "servicebinding-test-binding");
        put("application", application);
        put("services", services);
        put("envVarPrefix", "postgres");
        put("customEnvVar", customEnvVar);
      }
    };

    ServiceBindingConfig config = ServiceBindingConfigAdapter.adapt(map);

    assertNotNull(config);

    assertEquals("apps", config.getApplication().getGroup());
    assertEquals("Deployment", config.getApplication().getResource());
    assertEquals("v1alpha1", config.getApplication().getVersion());
    assertEquals("servicebinding-test", config.getApplication().getName());

    assertEquals(1, config.getServices().length);
    assertEquals("postgresql.dev", config.getServices()[0].getGroup());
    assertEquals("Database", config.getServices()[0].getKind());
    assertEquals("demo-database", config.getServices()[0].getName());
    assertEquals("postgresDB", config.getServices()[0].getId());

    assertEquals("foo", config.getCustomEnvVar()[0].getName());
    assertEquals("bar", config.getCustomEnvVar()[0].getValue());
    assertEquals("baz", config.getCustomEnvVar()[0].getField());
  }

  //  @Test
  //  public void testServiceBindingWithCustomEnvVarWithValueFromResourceFieldRef() {
  //
  //    final HashMap<String, Object> services[] = new HashMap[1];
  //    services[0] = new HashMap<String, Object>() {
  //      {
  //        put("group", "postgresql.dev");
  //        put("kind", "Database");
  //        put("name", "demo-database");
  //        put("id", "postgresDB");
  //      }
  //    };
  //
  //    final HashMap<String, Object> resourceFieldRef = new HashMap<String, Object>();
  //    resourceFieldRef.put("containerName", "foo");
  //    resourceFieldRef.put("divisor", "bar");
  //    resourceFieldRef.put("resource", "resource");
  //
  //    final HashMap<String, Object> valueFrom = new HashMap<String, Object>();
  //    valueFrom.put("resourceFieldRef", resourceFieldRef);
  //
  //    final HashMap<String, Object> customEnvVar[] = new HashMap[1];
  //    customEnvVar[0] = new HashMap<String, Object>() {
  //      {
  //        put("name", "foo");
  //        put("valueFrom", valueFrom);
  //      }
  //    };
  //
  //    final HashMap<String, Object> application = new HashMap<String, Object>();
  //    application.put("group", "apps");
  //    application.put("resource", "Deployment");
  //    application.put("version", "v1alpha1");
  //    application.put("name", "servicebinding-test");
  //
  //    Map<String, Object> map = new HashMap<String, Object>() {
  //      {
  //        put("name", "servicebinding-test-binding");
  //        put("application", application);
  //        put("services", services);
  //        put("envVarPrefix", "postgres");
  //        put("customEnvVar", customEnvVar);
  //      }
  //    };
  //
  //    ServiceBindingConfig config = ServiceBindingConfigAdapter.adapt(map);
  //
  //    assertNotNull(config);
  //
  //    assertEquals("apps", config.getApplication().getGroup());
  //    assertEquals("Deployment", config.getApplication().getResource());
  //    assertEquals("v1alpha1", config.getApplication().getVersion());
  //    assertEquals("servicebinding-test", config.getApplication().getName());
  //
  //    assertEquals(1, config.getServices().length);
  //    assertEquals("postgresql.dev", config.getServices()[0].getGroup());
  //    assertEquals("Database", config.getServices()[0].getKind());
  //    assertEquals("demo-database", config.getServices()[0].getName());
  //    assertEquals("postgresDB", config.getServices()[0].getId());
  //
  //    assertEquals("foo", config.getCustomEnvVar()[0].getValueFrom().getResourceFieldRef().getContainerName());
  //    assertEquals("bar", config.getCustomEnvVar()[0].getValueFrom().getResourceFieldRef().getDivisor());
  //    assertEquals("resource", config.getCustomEnvVar()[0].getValueFrom().getResourceFieldRef().getResource());
  //  }
}
