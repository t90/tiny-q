# TinyQ is a linq for java #

Linq is a first introduction to functional programming for one of mainstream languages C#, java lacks not built-in query language but also support from standard library. So here it is small subset on linq functionality.

No extra dependencies for your application, you can simply copy/paste a single file to your project.

## Usage example ##

First we need a collection of some data, let's say a set of strings

```

        String[] strings = {
                "bla",
                "mla",
                "bura",
                "bala",
                "mura",
                "buma"
        };


```

Now we want to select only the strings which start with "b"

```

Query<String> queryItems = (new Query<String>(strings)).where(
        new Query.Func<String,Boolean>(){
            public Boolean run(String in) {
                return in.startsWith("b");
            }
        }
);

```

No actual data moved copied or anything like that, it will get processed as soon as you start iterating

```

for(String s : queryItems){
    System.out.println(s);
}

```

Other ways to get your data is to call

```

List<String> list = queryItems.toList();

// or

int size = queryItems.size();

// or

String joined = queryItems.aggregate(new StringBuilder(), new Query.Accum<Query<String>, StringBuilder>() {
                    public StringBuilder run(Query<String> in, StringBuilder in2) {
                        in2.append(in);
                        return in2;
                    }
                }).toString();

```

Ok we know how to filter out some data, let's go to data mapping

```

class Person{
   public String name;
}

// ....

Query<Person> persons = queryItems.select(new Query.Func<String,Person>(){
    public String run(String in) {
        Person p = new Person();
        p.name = in;
        return p;
    }
});

```

Ok now we know how to filter out some data, how to transform it, next we will see how to map collection of collection to a single collection, for example turn array of filenames into a list of lines from each file.

```

String[] fileNames = new String[]{"log-2011-01-02.txt","log-2011-01-03.txt"};

List<String> logLines = (new Query<String>(fileNames)).selectMany(
    new Query.Func<String, Query<String>>(){
        public Query<String> run(String in) {
            return new Query<String>(new BufferedReaderIterator(new BufferedReader(new FileReader(in))));
        }
    }
).toList();

```

You always can combine calls into a chain, like

```
    selectMany(...).where().select().where().toList();
```

Enjoy!