# autoproxy

Autoproxy is an annotation post processor that autogenerates proxies for interfaces. For example annotating the following interface:

```java
@AutoProxy
public interface SayHello {
  void hi();
}
```

Will generate


```java
public final class AutoProxy_SayHello implements SayHello {
  
  private SayHello instance;
  
  public AutoProxy_SayHello(SayHello instance) {
    this.instance = instance;
  }
 
  public void hi() {
    instance.hi();
  }
  
}
```

Known limitations: Bounds on generics are not supported.
