package net.bytebuddy.dynamic.scaffold;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.test.utility.ObjectPropertyAssertion;
import org.junit.Test;

import static net.bytebuddy.matcher.ElementMatchers.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

public class MethodGraphCompilerDefaultTest {

    @Test
    public void testTrivialJavaHierarchy() throws Exception {
        MethodGraph.Linked methodGraph = MethodGraph.Compiler.Default.forJavaHierarchy().compile(TypeDescription.OBJECT);
        assertThat(methodGraph.listNodes().size(), is(TypeDescription.OBJECT.getDeclaredMethods().size()));
        assertThat(methodGraph.getSuperGraph().listNodes().size(), is(0));
        assertThat(methodGraph.getInterfaceGraph(mock(TypeDescription.class)).listNodes().size(), is(0));
        for (MethodDescription methodDescription : TypeDescription.OBJECT.getDeclaredMethods()) {
            MethodGraph.Node node = methodGraph.locate(methodDescription.asToken());
            assertThat(node.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
            assertThat(node.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
            assertThat(node.getBridges().size(), is(0));
            assertThat(node.getRepresentative(), is(methodDescription));
            assertThat(methodGraph.listNodes().contains(node), is(true));
        }
    }

    @Test
    public void testTrivialJVMHierarchy() throws Exception {
        MethodGraph.Linked methodGraph = MethodGraph.Compiler.Default.forJVMHierarchy().compile(TypeDescription.OBJECT);
        assertThat(methodGraph.listNodes().size(), is(TypeDescription.OBJECT.getDeclaredMethods().size()));
        assertThat(methodGraph.getSuperGraph().listNodes().size(), is(0));
        assertThat(methodGraph.getInterfaceGraph(mock(TypeDescription.class)).listNodes().size(), is(0));
        for (MethodDescription methodDescription : TypeDescription.OBJECT.getDeclaredMethods()) {
            MethodGraph.Node node = methodGraph.locate(methodDescription.asToken());
            assertThat(node.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
            assertThat(node.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
            assertThat(node.getBridges().size(), is(0));
            assertThat(node.getRepresentative(), is(methodDescription));
            assertThat(methodGraph.listNodes().contains(node), is(true));
        }
    }

    @Test
    public void testSimpleClass() throws Exception {
        TypeDescription typeDescription = new TypeDescription.ForLoadedType(SimpleClass.class);
        MethodGraph.Linked methodGraph = MethodGraph.Compiler.Default.forJavaHierarchy().compile(typeDescription);
        assertThat(methodGraph.listNodes().size(), is(TypeDescription.OBJECT.getDeclaredMethods().filter(isVirtual()).size() + 1));
        assertThat(methodGraph.getSuperGraph().listNodes().size(), is(TypeDescription.OBJECT.getDeclaredMethods().filter(isVirtual()).size()));
        assertThat(methodGraph.getInterfaceGraph(mock(TypeDescription.class)).listNodes().size(), is(0));
        for (MethodDescription methodDescription : TypeDescription.OBJECT.getDeclaredMethods().filter(isVirtual())) {
            MethodGraph.Node node = methodGraph.locate(methodDescription.asToken());
            assertThat(node.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
            assertThat(node.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
            assertThat(node.getBridges().size(), is(0));
            assertThat(node.getRepresentative(), is(methodDescription));
            assertThat(methodGraph.listNodes().contains(node), is(true));
        }
        MethodDescription constructor = typeDescription.getDeclaredMethods().filter(isConstructor()).getOnly();
        MethodGraph.Node constructorNode = methodGraph.locate(constructor.asToken());
        assertThat(constructorNode.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(constructorNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
        assertThat(constructorNode.getBridges().size(), is(0));
        assertThat(constructorNode.getRepresentative(), is(constructor));
        assertThat(methodGraph.listNodes().contains(constructorNode), is(true));
    }

    @Test
    public void testSimpleInterface() throws Exception {
        TypeDescription typeDescription = new TypeDescription.ForLoadedType(SimpleInterface.class);
        MethodGraph.Linked methodGraph = MethodGraph.Compiler.Default.forJavaHierarchy().compile(typeDescription);
        assertThat(methodGraph.listNodes().size(), is(0));
    }

    @Test
    public void testClassInheritance() throws Exception {
        TypeDescription typeDescription = new TypeDescription.ForLoadedType(ClassBase.Inner.class);
        MethodGraph.Linked methodGraph = MethodGraph.Compiler.Default.forJavaHierarchy().compile(typeDescription);
        assertThat(methodGraph.listNodes().size(), is(TypeDescription.OBJECT.getDeclaredMethods().filter(isVirtual()).size() + 2));
        MethodDescription method = typeDescription.getDeclaredMethods().filter(isMethod()).getOnly();
        MethodGraph.Node methodNode = methodGraph.locate(method.asToken());
        assertThat(methodNode.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(methodNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
        assertThat(methodNode.getBridges().size(), is(0));
        assertThat(methodNode.getRepresentative(), is(method));
        assertThat(methodGraph.listNodes().contains(methodNode), is(true));
        MethodGraph.Node baseNode = methodGraph.getSuperGraph().locate(method.asToken());
        assertThat(methodNode, not(is(baseNode)));
        assertThat(baseNode.getRepresentative(), is(typeDescription.getSuperType().getDeclaredMethods().filter(representedBy(method.asToken())).getOnly()));
    }

    @Test
    public void testInterfaceImplementation() throws Exception {
        TypeDescription typeDescription = new TypeDescription.ForLoadedType(InterfaceBase.InnerClass.class);
        MethodGraph.Linked methodGraph = MethodGraph.Compiler.Default.forJavaHierarchy().compile(typeDescription);
        assertThat(methodGraph.listNodes().size(), is(TypeDescription.OBJECT.getDeclaredMethods().filter(isVirtual()).size() + 2));
        MethodDescription method = typeDescription.getInterfaces().getOnly().getDeclaredMethods().getOnly();
        MethodGraph.Node methodNode = methodGraph.locate(method.asToken());
        assertThat(methodNode.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(methodNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
        assertThat(methodNode.getBridges().size(), is(0));
        assertThat(methodNode.getRepresentative(), is(method));
        assertThat(methodGraph.listNodes().contains(methodNode), is(true));
    }

    @Test
    public void testInterfaceExtension() throws Exception {
        TypeDescription typeDescription = new TypeDescription.ForLoadedType(InterfaceBase.InnerInterface.class);
        MethodGraph.Linked methodGraph = MethodGraph.Compiler.Default.forJavaHierarchy().compile(typeDescription);
        assertThat(methodGraph.listNodes().size(), is(1));
        MethodDescription method = typeDescription.getInterfaces().getOnly().getDeclaredMethods().getOnly();
        MethodGraph.Node methodNode = methodGraph.locate(method.asToken());
        assertThat(methodNode.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(methodNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
        assertThat(methodNode.getBridges().size(), is(0));
        assertThat(methodNode.getRepresentative(), is(method));
        assertThat(methodGraph.listNodes().contains(methodNode), is(true));
    }

    @Test
    public void testClassAndInterfaceDominantInheritance() throws Exception {
        TypeDescription typeDescription = new TypeDescription.ForLoadedType(ClassAndInterfaceInheritance.class);
        MethodGraph.Linked methodGraph = MethodGraph.Compiler.Default.forJavaHierarchy().compile(typeDescription);
        assertThat(methodGraph.listNodes().size(), is(TypeDescription.OBJECT.getDeclaredMethods().filter(isVirtual()).size() + 2));
        MethodDescription method = typeDescription.getSuperType().getDeclaredMethods().filter(isMethod()).getOnly();
        MethodGraph.Node methodNode = methodGraph.locate(method.asToken());
        assertThat(methodNode.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(methodNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
        assertThat(methodNode.getBridges().size(), is(0));
        assertThat(methodNode.getRepresentative(), is(method));
        MethodGraph.Node baseNode = methodGraph.getInterfaceGraph(new TypeDescription.ForLoadedType(InterfaceBase.class)).locate(method.asToken());
        assertThat(methodNode, not(is(baseNode)));
        assertThat(baseNode.getRepresentative(), is(typeDescription.getInterfaces().getOnly().getDeclaredMethods().getOnly()));
    }

    @Test
    public void testMultipleAmbiguousClassInheritance() throws Exception {
        TypeDescription typeDescription = new TypeDescription.ForLoadedType(AmbiguousInterfaceBase.ClassTarget.class);
        MethodGraph.Linked methodGraph = MethodGraph.Compiler.Default.forJavaHierarchy().compile(typeDescription);
        assertThat(methodGraph.listNodes().size(), is(TypeDescription.OBJECT.getDeclaredMethods().filter(isVirtual()).size() + 2));
        MethodDescription first = typeDescription.getInterfaces().filter(rawType(InterfaceBase.class)).getOnly()
                .getDeclaredMethods().filter(isMethod()).getOnly();
        MethodDescription second = typeDescription.getInterfaces().filter(rawType(AmbiguousInterfaceBase.class)).getOnly()
                .getDeclaredMethods().filter(isMethod()).getOnly();
        MethodGraph.Node methodNode = methodGraph.locate(first.asToken());
        assertThat(methodNode.getSort(), is(MethodGraph.Node.Sort.AMBIGUOUS));
        assertThat(methodNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
        assertThat(methodNode.getBridges().size(), is(0));
        assertThat(methodNode.getRepresentative(), is(first));
        assertThat(methodNode.getRepresentative(), not(second));
        assertThat(methodNode, is(methodGraph.locate(second.asToken())));
        MethodGraph.Node firstBaseNode = methodGraph.getInterfaceGraph(new TypeDescription.ForLoadedType(InterfaceBase.class)).locate(first.asToken());
        assertThat(methodNode, not(is(firstBaseNode)));
        assertThat(firstBaseNode.getRepresentative(), is(first));
        MethodGraph.Node secondBaseNode = methodGraph.getInterfaceGraph(new TypeDescription.ForLoadedType(InterfaceBase.class)).locate(second.asToken());
        assertThat(methodNode, not(is(secondBaseNode)));
        assertThat(secondBaseNode.getRepresentative(), is(first));
    }

    @Test
    public void testMultipleAmbiguousInterfaceInheritance() throws Exception {
        TypeDescription typeDescription = new TypeDescription.ForLoadedType(AmbiguousInterfaceBase.InterfaceTarget.class);
        MethodGraph.Linked methodGraph = MethodGraph.Compiler.Default.forJavaHierarchy().compile(typeDescription);
        assertThat(methodGraph.listNodes().size(), is(1));
        MethodDescription first = typeDescription.getInterfaces().filter(rawType(InterfaceBase.class)).getOnly()
                .getDeclaredMethods().filter(isMethod()).getOnly();
        MethodDescription second = typeDescription.getInterfaces().filter(rawType(AmbiguousInterfaceBase.class)).getOnly()
                .getDeclaredMethods().filter(isMethod()).getOnly();
        MethodGraph.Node methodNode = methodGraph.locate(first.asToken());
        assertThat(methodNode.getSort(), is(MethodGraph.Node.Sort.AMBIGUOUS));
        assertThat(methodNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
        assertThat(methodNode.getBridges().size(), is(0));
        assertThat(methodNode.getRepresentative(), is(first));
        assertThat(methodNode.getRepresentative(), not(second));
        assertThat(methodNode, is(methodGraph.locate(second.asToken())));
        MethodGraph.Node firstBaseNode = methodGraph.getInterfaceGraph(new TypeDescription.ForLoadedType(InterfaceBase.class)).locate(first.asToken());
        assertThat(methodNode, not(is(firstBaseNode)));
        assertThat(firstBaseNode.getRepresentative(), is(first));
        MethodGraph.Node secondBaseNode = methodGraph.getInterfaceGraph(new TypeDescription.ForLoadedType(InterfaceBase.class)).locate(second.asToken());
        assertThat(methodNode, not(is(secondBaseNode)));
        assertThat(secondBaseNode.getRepresentative(), is(first));
    }

    @Test
    public void testDominantClassInheritance() throws Exception {
        TypeDescription typeDescription = new TypeDescription.ForLoadedType(AmbiguousInterfaceBase.DominantClassTarget.class);
        MethodGraph.Linked methodGraph = MethodGraph.Compiler.Default.forJavaHierarchy().compile(typeDescription);
        assertThat(methodGraph.listNodes().size(), is(TypeDescription.OBJECT.getDeclaredMethods().filter(isVirtual()).size() + 2));
        MethodDescription methodDescription = new TypeDescription.ForLoadedType(AmbiguousInterfaceBase.DominantIntermediate.class)
                .getDeclaredMethods().getOnly();
        MethodGraph.Node methodNode = methodGraph.locate(methodDescription.asToken());
        assertThat(methodNode.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(methodNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
        assertThat(methodNode.getBridges().size(), is(0));
        assertThat(methodNode.getRepresentative(), is(methodDescription));
    }

    @Test
    public void testDominantInterfaceInheritanceLeft() throws Exception {
        TypeDescription typeDescription = new TypeDescription.ForLoadedType(AmbiguousInterfaceBase.DominantInterfaceTargetLeft.class);
        MethodGraph.Linked methodGraph = MethodGraph.Compiler.Default.forJavaHierarchy().compile(typeDescription);
        assertThat(methodGraph.listNodes().size(), is(1));
        MethodDescription methodDescription = new TypeDescription.ForLoadedType(AmbiguousInterfaceBase.DominantIntermediate.class)
                .getDeclaredMethods().getOnly();
        MethodGraph.Node methodNode = methodGraph.locate(methodDescription.asToken());
        assertThat(methodNode.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(methodNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
        assertThat(methodNode.getBridges().size(), is(0));
        assertThat(methodNode.getRepresentative(), is(methodDescription));
    }

    @Test
    public void testDominantInterfaceInheritanceRight() throws Exception {
        TypeDescription typeDescription = new TypeDescription.ForLoadedType(AmbiguousInterfaceBase.DominantInterfaceTargetRight.class);
        MethodGraph.Linked methodGraph = MethodGraph.Compiler.Default.forJavaHierarchy().compile(typeDescription);
        assertThat(methodGraph.listNodes().size(), is(1));
        MethodDescription methodDescription = new TypeDescription.ForLoadedType(AmbiguousInterfaceBase.DominantIntermediate.class)
                .getDeclaredMethods().getOnly();
        MethodGraph.Node methodNode = methodGraph.locate(methodDescription.asToken());
        assertThat(methodNode.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(methodNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
        assertThat(methodNode.getBridges().size(), is(0));
        assertThat(methodNode.getRepresentative(), is(methodDescription));
    }

    @Test
    public void testNonDominantInterfaceInheritanceLeft() throws Exception {
        TypeDescription typeDescription = new TypeDescription.ForLoadedType(AmbiguousInterfaceBase.NonDominantTargetLeft.class);
        MethodGraph.Linked methodGraph = MethodGraph.Compiler.Default.forJavaHierarchy().compile(typeDescription);
        assertThat(methodGraph.listNodes().size(), is(1));
        MethodDescription methodDescription = new TypeDescription.ForLoadedType(AmbiguousInterfaceBase.DominantIntermediate.class)
                .getDeclaredMethods().getOnly();
        MethodGraph.Node methodNode = methodGraph.locate(methodDescription.asToken());
        assertThat(methodNode.getSort(), is(MethodGraph.Node.Sort.AMBIGUOUS));
        assertThat(methodNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
        assertThat(methodNode.getBridges().size(), is(0));
        assertThat(methodNode.getRepresentative(), is(methodDescription));
    }

    @Test
    public void testNonDominantInterfaceInheritanceRight() throws Exception {
        TypeDescription typeDescription = new TypeDescription.ForLoadedType(AmbiguousInterfaceBase.NonDominantTargetRight.class);
        MethodGraph.Linked methodGraph = MethodGraph.Compiler.Default.forJavaHierarchy().compile(typeDescription);
        assertThat(methodGraph.listNodes().size(), is(1));
        MethodDescription methodDescription = new TypeDescription.ForLoadedType(AmbiguousInterfaceBase.DominantIntermediate.class)
                .getDeclaredMethods().getOnly();
        MethodGraph.Node methodNode = methodGraph.locate(methodDescription.asToken());
        assertThat(methodNode.getSort(), is(MethodGraph.Node.Sort.AMBIGUOUS));
        assertThat(methodNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
        assertThat(methodNode.getBridges().size(), is(0));
        assertThat(methodNode.getRepresentative(), is(methodDescription));
    }

    @Test
    public void testGenericClassSingleEvolution() throws Exception {
        TypeDescription typeDescription = new TypeDescription.ForLoadedType(GenericClassBase.Inner.class);
        MethodGraph.Linked methodGraph = MethodGraph.Compiler.Default.forJavaHierarchy().compile(typeDescription);
        assertThat(methodGraph.listNodes().size(), is(TypeDescription.OBJECT.getDeclaredMethods().filter(isVirtual()).size() + 2));
        MethodGraph.Node methodNode = methodGraph.locate(typeDescription.getDeclaredMethods()
                .filter(isMethod().and(ElementMatchers.not(isBridge()))).getOnly().asToken());
        MethodDescription.Token bridgeToken = typeDescription.getSuperType().getDeclaredMethods().filter(isMethod()).getOnly().asDefined().asToken();
        assertThat(methodNode, is(methodGraph.locate(bridgeToken)));
        assertThat(methodNode.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(methodNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
        assertThat(methodNode.getBridges().size(), is(1));
        assertThat(methodNode.getBridges().contains(bridgeToken.asTypeToken()), is(true));
    }

    @Test
    public void testGenericClassMultipleEvolution() throws Exception {
        TypeDescription typeDescription = new TypeDescription.ForLoadedType(GenericClassBase.Intermediate.Inner.class);
        MethodGraph.Linked methodGraph = MethodGraph.Compiler.Default.forJavaHierarchy().compile(typeDescription);
        assertThat(methodGraph.listNodes().size(), is(TypeDescription.OBJECT.getDeclaredMethods().filter(isVirtual()).size() + 2));
        MethodGraph.Node methodNode = methodGraph.locate(typeDescription.getDeclaredMethods()
                .filter(isMethod().and(ElementMatchers.not(isBridge()))).getOnly().asToken());
        MethodDescription.Token firstBridgeToken = typeDescription.getSuperType().getDeclaredMethods()
                .filter(isMethod().and(ElementMatchers.not(isBridge()))).getOnly().asDefined().asToken();
        MethodDescription.Token secondBridgeToken = typeDescription.getSuperType().getSuperType().getDeclaredMethods()
                .filter(isMethod()).getOnly().asDefined().asToken();
        assertThat(methodNode, is(methodGraph.locate(firstBridgeToken)));
        assertThat(methodNode, is(methodGraph.locate(secondBridgeToken)));
        assertThat(methodNode.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(methodNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
        assertThat(methodNode.getBridges().size(), is(2));
        assertThat(methodNode.getBridges().contains(firstBridgeToken.asTypeToken()), is(true));
        assertThat(methodNode.getBridges().contains(secondBridgeToken.asTypeToken()), is(true));
    }

    @Test
    public void testReturnTypeClassSingleEvolution() throws Exception {
        TypeDescription typeDescription = new TypeDescription.ForLoadedType(ReturnTypeClassBase.Inner.class);
        MethodGraph.Linked methodGraph = MethodGraph.Compiler.Default.forJavaHierarchy().compile(typeDescription);
        assertThat(methodGraph.listNodes().size(), is(TypeDescription.OBJECT.getDeclaredMethods().filter(isVirtual()).size() + 2));
        MethodGraph.Node methodNode = methodGraph.locate(typeDescription.getDeclaredMethods()
                .filter(isMethod().and(ElementMatchers.not(isBridge()))).getOnly().asToken());
        MethodDescription.Token bridgeToken = typeDescription.getSuperType().getDeclaredMethods().filter(isMethod()).getOnly().asToken();
        assertThat(methodNode, is(methodGraph.locate(bridgeToken)));
        assertThat(methodNode.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(methodNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
        assertThat(methodNode.getBridges().size(), is(1));
        assertThat(methodNode.getBridges().contains(bridgeToken.asTypeToken()), is(true));
    }

    @Test
    public void testReturnTypeClassMultipleEvolution() throws Exception {
        TypeDescription typeDescription = new TypeDescription.ForLoadedType(ReturnTypeClassBase.Intermediate.Inner.class);
        MethodGraph.Linked methodGraph = MethodGraph.Compiler.Default.forJavaHierarchy().compile(typeDescription);
        assertThat(methodGraph.listNodes().size(), is(TypeDescription.OBJECT.getDeclaredMethods().filter(isVirtual()).size() + 2));
        MethodGraph.Node methodNode = methodGraph.locate(typeDescription.getDeclaredMethods()
                .filter(isMethod().and(ElementMatchers.not(isBridge()))).getOnly().asToken());
        MethodDescription.Token firstBridgeToken = typeDescription.getSuperType().getDeclaredMethods()
                .filter(isMethod().and(ElementMatchers.not(isBridge()))).getOnly().asDefined().asToken();
        MethodDescription.Token secondBridgeToken = typeDescription.getSuperType().getSuperType().getDeclaredMethods()
                .filter(isMethod()).getOnly().asDefined().asToken();
        assertThat(methodNode, is(methodGraph.locate(firstBridgeToken)));
        assertThat(methodNode, is(methodGraph.locate(secondBridgeToken)));
        assertThat(methodNode.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(methodNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
        assertThat(methodNode.getBridges().size(), is(2));
        assertThat(methodNode.getBridges().contains(firstBridgeToken.asTypeToken()), is(true));
        assertThat(methodNode.getBridges().contains(secondBridgeToken.asTypeToken()), is(true));
    }

    @Test
    public void testGenericInterfaceSingleEvolution() throws Exception {
        TypeDescription typeDescription = new TypeDescription.ForLoadedType(GenericInterfaceBase.Inner.class);
        MethodGraph.Linked methodGraph = MethodGraph.Compiler.Default.forJavaHierarchy().compile(typeDescription);
        assertThat(methodGraph.listNodes().size(), is(1));
        MethodGraph.Node methodNode = methodGraph.locate(typeDescription.getDeclaredMethods()
                .filter(isMethod().and(ElementMatchers.not(isBridge()))).getOnly().asToken());
        MethodDescription.Token bridgeToken = typeDescription.getInterfaces().getOnly()
                .getDeclaredMethods().filter(isMethod()).getOnly().asDefined().asToken();
        assertThat(methodNode, is(methodGraph.locate(bridgeToken)));
        assertThat(methodNode.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(methodNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
        assertThat(methodNode.getBridges().size(), is(1));
        assertThat(methodNode.getBridges().contains(bridgeToken.asTypeToken()), is(true));
    }

    @Test
    public void testGenericInterfaceMultipleEvolution() throws Exception {
        TypeDescription typeDescription = new TypeDescription.ForLoadedType(GenericInterfaceBase.Intermediate.Inner.class);
        MethodGraph.Linked methodGraph = MethodGraph.Compiler.Default.forJavaHierarchy().compile(typeDescription);
        assertThat(methodGraph.listNodes().size(), is(1));
        MethodGraph.Node methodNode = methodGraph.locate(typeDescription.getDeclaredMethods().getOnly().asToken());
        MethodDescription.Token firstBridgeToken = typeDescription.getInterfaces().getOnly()
                .getDeclaredMethods().getOnly().asDefined().asToken();
        MethodDescription.Token secondBridgeToken = typeDescription.getInterfaces().getOnly().getInterfaces().getOnly()
                .getDeclaredMethods().getOnly().asDefined().asToken();
        assertThat(methodNode, is(methodGraph.locate(firstBridgeToken)));
        assertThat(methodNode, is(methodGraph.locate(secondBridgeToken)));
        assertThat(methodNode.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(methodNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
        assertThat(methodNode.getBridges().size(), is(2));
        assertThat(methodNode.getBridges().contains(firstBridgeToken.asTypeToken()), is(true));
        assertThat(methodNode.getBridges().contains(secondBridgeToken.asTypeToken()), is(true));
    }

    @Test
    public void testReturnTypeInterfaceSingleEvolution() throws Exception {
        TypeDescription typeDescription = new TypeDescription.ForLoadedType(ReturnTypeInterfaceBase.Inner.class);
        MethodGraph.Linked methodGraph = MethodGraph.Compiler.Default.forJavaHierarchy().compile(typeDescription);
        assertThat(methodGraph.listNodes().size(), is(1));
        MethodGraph.Node methodNode = methodGraph.locate(typeDescription.getDeclaredMethods().getOnly().asToken());
        MethodDescription.Token bridgeToken = typeDescription.getInterfaces().getOnly().getDeclaredMethods().getOnly().asToken();
        assertThat(methodNode, is(methodGraph.locate(bridgeToken)));
        assertThat(methodNode.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(methodNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
        assertThat(methodNode.getBridges().size(), is(1));
        assertThat(methodNode.getBridges().contains(bridgeToken.asTypeToken()), is(true));
    }

    @Test
    public void testReturnTypeInterfaceMultipleEvolution() throws Exception {
        TypeDescription typeDescription = new TypeDescription.ForLoadedType(ReturnTypeInterfaceBase.Intermediate.Inner.class);
        MethodGraph.Linked methodGraph = MethodGraph.Compiler.Default.forJavaHierarchy().compile(typeDescription);
        assertThat(methodGraph.listNodes().size(), is(1));
        MethodGraph.Node methodNode = methodGraph.locate(typeDescription.getDeclaredMethods().getOnly().asToken());
        MethodDescription.Token firstBridgeToken = typeDescription.getInterfaces().getOnly()
                .getDeclaredMethods().getOnly().asToken();
        MethodDescription.Token secondBridgeToken = typeDescription.getInterfaces().getOnly().getInterfaces().getOnly()
                .getDeclaredMethods().getOnly().asToken();
        assertThat(methodNode, is(methodGraph.locate(firstBridgeToken)));
        assertThat(methodNode.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(methodNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
        assertThat(methodNode.getBridges().size(), is(2));
        assertThat(methodNode.getBridges().contains(firstBridgeToken.asTypeToken()), is(true));
        assertThat(methodNode.getBridges().contains(secondBridgeToken.asTypeToken()), is(true));
    }

    @Test
    public void testGenericWithReturnTypeClassSingleEvolution() throws Exception {
        TypeDescription typeDescription = new TypeDescription.ForLoadedType(GenericWithReturnTypeClassBase.Inner.class);
        MethodGraph.Linked methodGraph = MethodGraph.Compiler.Default.forJavaHierarchy().compile(typeDescription);
        assertThat(methodGraph.listNodes().size(), is(TypeDescription.OBJECT.getDeclaredMethods().filter(isVirtual()).size() + 2));
        MethodGraph.Node methodNode = methodGraph.locate(typeDescription.getDeclaredMethods()
                .filter(isMethod().and(ElementMatchers.not(isBridge()))).getOnly().asToken());
        MethodDescription.Token bridgeToken = typeDescription.getSuperType().getDeclaredMethods().filter(isMethod()).getOnly().asDefined().asToken();
        assertThat(methodNode, is(methodGraph.locate(bridgeToken)));
        assertThat(methodNode.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(methodNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
        assertThat(methodNode.getBridges().size(), is(1));
        assertThat(methodNode.getBridges().contains(bridgeToken.asTypeToken()), is(true));
    }

    @Test
    public void testGenericWithReturnTypeClassMultipleEvolution() throws Exception {
        TypeDescription typeDescription = new TypeDescription.ForLoadedType(GenericWithReturnTypeClassBase.Intermediate.Inner.class);
        MethodGraph.Linked methodGraph = MethodGraph.Compiler.Default.forJavaHierarchy().compile(typeDescription);
        assertThat(methodGraph.listNodes().size(), is(TypeDescription.OBJECT.getDeclaredMethods().filter(isVirtual()).size() + 2));
        MethodGraph.Node methodNode = methodGraph.locate(typeDescription.getDeclaredMethods()
                .filter(isMethod().and(ElementMatchers.not(isBridge()))).getOnly().asToken());
        MethodDescription.Token firstBridgeToken = typeDescription.getSuperType().getDeclaredMethods()
                .filter(isMethod().and(ElementMatchers.not(isBridge()))).getOnly().asDefined().asToken();
        MethodDescription.Token secondBridgeToken = typeDescription.getSuperType().getSuperType().getDeclaredMethods()
                .filter(isMethod()).getOnly().asDefined().asToken();
        assertThat(methodNode, is(methodGraph.locate(firstBridgeToken)));
        assertThat(methodNode, is(methodGraph.locate(secondBridgeToken)));
        assertThat(methodNode.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(methodNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
        assertThat(methodNode.getBridges().size(), is(2));
        assertThat(methodNode.getBridges().contains(firstBridgeToken.asTypeToken()), is(true));
        assertThat(methodNode.getBridges().contains(secondBridgeToken.asTypeToken()), is(true));
    }

    @Test
    public void testGenericWithReturnTypeInterfaceSingleEvolution() throws Exception {
        TypeDescription typeDescription = new TypeDescription.ForLoadedType(GenericWithReturnTypeInterfaceBase.Inner.class);
        MethodGraph.Linked methodGraph = MethodGraph.Compiler.Default.forJavaHierarchy().compile(typeDescription);
        assertThat(methodGraph.listNodes().size(), is(1));
        MethodGraph.Node methodNode = methodGraph.locate(typeDescription.getDeclaredMethods()
                .filter(isMethod().and(ElementMatchers.not(isBridge()))).getOnly().asToken());
        MethodDescription.Token bridgeToken = typeDescription.getInterfaces().getOnly()
                .getDeclaredMethods().filter(isMethod()).getOnly().asDefined().asToken();
        assertThat(methodNode, is(methodGraph.locate(bridgeToken)));
        assertThat(methodNode.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(methodNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
        assertThat(methodNode.getBridges().size(), is(1));
        assertThat(methodNode.getBridges().contains(bridgeToken.asTypeToken()), is(true));
    }

    @Test
    public void testGenericWithReturnTypeInterfaceMultipleEvolution() throws Exception {
        TypeDescription typeDescription = new TypeDescription.ForLoadedType(GenericWithReturnTypeInterfaceBase.Intermediate.Inner.class);
        MethodGraph.Linked methodGraph = MethodGraph.Compiler.Default.forJavaHierarchy().compile(typeDescription);
        assertThat(methodGraph.listNodes().size(), is(1));
        MethodGraph.Node methodNode = methodGraph.locate(typeDescription.getDeclaredMethods().getOnly().asToken());
        MethodDescription.Token firstBridgeToken = typeDescription.getInterfaces().getOnly()
                .getDeclaredMethods().getOnly().asDefined().asToken();
        MethodDescription.Token secondBridgeToken = typeDescription.getInterfaces().getOnly().getInterfaces().getOnly()
                .getDeclaredMethods().getOnly().asDefined().asToken();
        assertThat(methodNode, is(methodGraph.locate(firstBridgeToken)));
        assertThat(methodNode, is(methodGraph.locate(secondBridgeToken)));
        assertThat(methodNode.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(methodNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
        assertThat(methodNode.getBridges().size(), is(2));
        assertThat(methodNode.getBridges().contains(firstBridgeToken.asTypeToken()), is(true));
        assertThat(methodNode.getBridges().contains(secondBridgeToken.asTypeToken()), is(true));
    }

    @Test
    public void testGenericNonOverriddenClassExtension() throws Exception {
        TypeDescription typeDescription = new TypeDescription.ForLoadedType(GenericNonOverriddenClassBase.Inner.class);
        MethodGraph.Linked methodGraph = MethodGraph.Compiler.Default.forJavaHierarchy().compile(typeDescription);
        assertThat(methodGraph.listNodes().size(), is(TypeDescription.OBJECT.getDeclaredMethods().filter(isVirtual()).size() + 2));
        MethodDescription methodDescription = typeDescription.getSuperType().getDeclaredMethods().filter(isMethod()).getOnly();
        MethodGraph.Node node = methodGraph.locate(methodDescription.asToken());
        assertThat(node.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(node.getRepresentative(), is(methodDescription));
        assertThat(node.getBridges().size(), is(1));
        assertThat(node.getBridges().contains(methodDescription.asDefined().asTypeToken()), is(true));
        assertThat(node.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
        assertThat(node, is(methodGraph.getSuperGraph().locate(methodDescription.asToken())));
    }

    @Test
    public void testGenericNonOverriddenInterfaceExtension() throws Exception {
        TypeDescription typeDescription = new TypeDescription.ForLoadedType(GenericNonOverriddenInterfaceBase.InnerClass.class);
        MethodGraph.Linked methodGraph = MethodGraph.Compiler.Default.forJavaHierarchy().compile(typeDescription);
        assertThat(methodGraph.listNodes().size(), is(TypeDescription.OBJECT.getDeclaredMethods().filter(isVirtual()).size() + 2));
        MethodDescription methodDescription = typeDescription.getInterfaces().getOnly().getDeclaredMethods().filter(isMethod()).getOnly();
        MethodGraph.Node node = methodGraph.locate(methodDescription.asToken());
        assertThat(node.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(node.getRepresentative(), is(methodDescription));
        assertThat(node.getBridges().size(), is(1));
        assertThat(node.getBridges().contains(methodDescription.asDefined().asTypeToken()), is(true));
        assertThat(node.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
        assertThat(node, is(methodGraph.getInterfaceGraph(new TypeDescription.ForLoadedType(GenericNonOverriddenInterfaceBase.class))
                .locate(methodDescription.asToken())));
    }

    @Test
    public void testGenericNonOverriddenInterfaceImplementation() throws Exception {
        TypeDescription typeDescription = new TypeDescription.ForLoadedType(GenericNonOverriddenInterfaceBase.InnerInterface.class);
        MethodGraph.Linked methodGraph = MethodGraph.Compiler.Default.forJavaHierarchy().compile(typeDescription);
        assertThat(methodGraph.listNodes().size(), is(1));
        MethodDescription methodDescription = typeDescription.getInterfaces().getOnly().getDeclaredMethods().filter(isMethod()).getOnly();
        MethodGraph.Node node = methodGraph.locate(methodDescription.asToken());
        assertThat(node.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(node.getRepresentative(), is(methodDescription));
        assertThat(node.getBridges().size(), is(1));
        assertThat(node.getBridges().contains(methodDescription.asDefined().asTypeToken()), is(true));
        assertThat(node.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
        assertThat(node, is(methodGraph.getInterfaceGraph(new TypeDescription.ForLoadedType(GenericNonOverriddenInterfaceBase.class))
                .locate(methodDescription.asToken())));
    }

    @Test
    public void testDuplicateNameClass() throws Exception {
        TypeDescription typeDescription = new TypeDescription.ForLoadedType(DuplicateNameClass.class);
        MethodGraph.Linked methodGraph = MethodGraph.Compiler.Default.forJavaHierarchy().compile(typeDescription);
        assertThat(methodGraph.listNodes().size(), is(TypeDescription.OBJECT.getDeclaredMethods().filter(isVirtual()).size() + 3));
        MethodDescription objectMethod = typeDescription.getDeclaredMethods().filter(takesArguments(Object.class)).getOnly();
        MethodGraph.Node objectNode = methodGraph.locate(objectMethod.asToken());
        assertThat(objectNode.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(objectNode.getRepresentative(), is(objectMethod));
        assertThat(objectNode.getBridges().size(), is(0));
        assertThat(objectNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
        MethodDescription voidMethod = typeDescription.getDeclaredMethods().filter(takesArguments(Integer.class)).getOnly();
        MethodGraph.Node voidNode = methodGraph.locate(voidMethod.asToken());
        assertThat(voidNode.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(voidNode.getRepresentative(), is(voidMethod));
        assertThat(voidNode.getBridges().size(), is(0));
        assertThat(voidNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
    }

    @Test
    public void testDuplicateNameClassExtension() throws Exception {
        TypeDescription typeDescription = new TypeDescription.ForLoadedType(DuplicateNameClass.Inner.class);
        MethodGraph.Linked methodGraph = MethodGraph.Compiler.Default.forJavaHierarchy().compile(typeDescription);
        assertThat(methodGraph.listNodes().size(), is(TypeDescription.OBJECT.getDeclaredMethods().filter(isVirtual()).size() + 4));
        MethodDescription objectMethod = typeDescription.getSuperType().getDeclaredMethods().filter(takesArguments(Object.class)).getOnly();
        MethodGraph.Node objectNode = methodGraph.locate(objectMethod.asToken());
        assertThat(objectNode.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(objectNode.getRepresentative(), is(objectMethod));
        assertThat(objectNode.getBridges().size(), is(0));
        assertThat(objectNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
        MethodDescription integerMethod = typeDescription.getSuperType().getDeclaredMethods().filter(takesArguments(Integer.class)).getOnly();
        MethodGraph.Node integerNode = methodGraph.locate(integerMethod.asToken());
        assertThat(integerNode.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(integerNode.getRepresentative(), is(integerMethod));
        assertThat(integerNode.getBridges().size(), is(0));
        assertThat(integerNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
        MethodDescription voidMethod = typeDescription.getDeclaredMethods().filter(takesArguments(Void.class)).getOnly();
        MethodGraph.Node voidNode = methodGraph.locate(voidMethod.asToken());
        assertThat(voidNode.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(voidNode.getRepresentative(), is(voidMethod));
        assertThat(voidNode.getBridges().size(), is(0));
        assertThat(voidNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
    }

    @Test
    public void testDuplicateNameInterface() throws Exception {
        TypeDescription typeDescription = new TypeDescription.ForLoadedType(DuplicateNameInterface.class);
        MethodGraph.Linked methodGraph = MethodGraph.Compiler.Default.forJavaHierarchy().compile(typeDescription);
        assertThat(methodGraph.listNodes().size(), is(2));
        MethodDescription objectMethod = typeDescription.getDeclaredMethods().filter(takesArguments(Object.class)).getOnly();
        MethodGraph.Node objectNode = methodGraph.locate(objectMethod.asToken());
        assertThat(objectNode.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(objectNode.getRepresentative(), is(objectMethod));
        assertThat(objectNode.getBridges().size(), is(0));
        assertThat(objectNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
        MethodDescription voidMethod = typeDescription.getDeclaredMethods().filter(takesArguments(Integer.class)).getOnly();
        MethodGraph.Node voidNode = methodGraph.locate(voidMethod.asToken());
        assertThat(voidNode.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(voidNode.getRepresentative(), is(voidMethod));
        assertThat(voidNode.getBridges().size(), is(0));
        assertThat(voidNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
    }

    @Test
    public void testDuplicateNameInterfaceImplementation() throws Exception {
        TypeDescription typeDescription = new TypeDescription.ForLoadedType(DuplicateNameInterface.InnerClass.class);
        MethodGraph.Linked methodGraph = MethodGraph.Compiler.Default.forJavaHierarchy().compile(typeDescription);
        assertThat(methodGraph.listNodes().size(), is(TypeDescription.OBJECT.getDeclaredMethods().filter(isVirtual()).size() + 4));
        MethodDescription objectMethod = typeDescription.getInterfaces().getOnly().getDeclaredMethods().filter(takesArguments(Object.class)).getOnly();
        MethodGraph.Node objectNode = methodGraph.locate(objectMethod.asToken());
        assertThat(objectNode.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(objectNode.getRepresentative(), is(objectMethod));
        assertThat(objectNode.getBridges().size(), is(0));
        assertThat(objectNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
        MethodDescription integerMethod = typeDescription.getInterfaces().getOnly().getDeclaredMethods().filter(takesArguments(Integer.class)).getOnly();
        MethodGraph.Node integerNode = methodGraph.locate(integerMethod.asToken());
        assertThat(integerNode.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(integerNode.getRepresentative(), is(integerMethod));
        assertThat(integerNode.getBridges().size(), is(0));
        assertThat(integerNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
        MethodDescription voidMethod = typeDescription.getDeclaredMethods().filter(takesArguments(Void.class)).getOnly();
        MethodGraph.Node voidNode = methodGraph.locate(voidMethod.asToken());
        assertThat(voidNode.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(voidNode.getRepresentative(), is(voidMethod));
        assertThat(voidNode.getBridges().size(), is(0));
        assertThat(voidNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
    }

    @Test
    public void testDuplicateNameInterfaceExtension() throws Exception {
        TypeDescription typeDescription = new TypeDescription.ForLoadedType(DuplicateNameInterface.InnerInterface.class);
        MethodGraph.Linked methodGraph = MethodGraph.Compiler.Default.forJavaHierarchy().compile(typeDescription);
        assertThat(methodGraph.listNodes().size(), is(3));
        MethodDescription objectMethod = typeDescription.getInterfaces().getOnly().getDeclaredMethods().filter(takesArguments(Object.class)).getOnly();
        MethodGraph.Node objectNode = methodGraph.locate(objectMethod.asToken());
        assertThat(objectNode.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(objectNode.getRepresentative(), is(objectMethod));
        assertThat(objectNode.getBridges().size(), is(0));
        assertThat(objectNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
        MethodDescription integerMethod = typeDescription.getInterfaces().getOnly().getDeclaredMethods().filter(takesArguments(Integer.class)).getOnly();
        MethodGraph.Node integerNode = methodGraph.locate(integerMethod.asToken());
        assertThat(integerNode.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(integerNode.getRepresentative(), is(integerMethod));
        assertThat(integerNode.getBridges().size(), is(0));
        assertThat(integerNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
        MethodDescription voidMethod = typeDescription.getDeclaredMethods().filter(takesArguments(Void.class)).getOnly();
        MethodGraph.Node voidNode = methodGraph.locate(voidMethod.asToken());
        assertThat(voidNode.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(voidNode.getRepresentative(), is(voidMethod));
        assertThat(voidNode.getBridges().size(), is(0));
        assertThat(voidNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
    }

    @Test
    public void testDuplicateNameGenericClass() throws Exception {
        TypeDescription typeDescription = new TypeDescription.ForLoadedType(DuplicateNameGenericClass.class);
        MethodGraph.Linked methodGraph = MethodGraph.Compiler.Default.forJavaHierarchy().compile(typeDescription);
        assertThat(methodGraph.listNodes().size(), is(TypeDescription.OBJECT.getDeclaredMethods().filter(isVirtual()).size() + 3));
        MethodDescription objectMethod = typeDescription.getDeclaredMethods().filter(takesArguments(Object.class)).getOnly();
        MethodGraph.Node objectNode = methodGraph.locate(objectMethod.asToken());
        assertThat(objectNode.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(objectNode.getRepresentative(), is(objectMethod));
        assertThat(objectNode.getBridges().size(), is(0));
        assertThat(objectNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
        MethodDescription voidMethod = typeDescription.getDeclaredMethods().filter(takesArguments(Integer.class)).getOnly();
        MethodGraph.Node voidNode = methodGraph.locate(voidMethod.asToken());
        assertThat(voidNode.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(voidNode.getRepresentative(), is(voidMethod));
        assertThat(voidNode.getBridges().size(), is(0));
        assertThat(voidNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
    }

    @Test
    public void testDuplicateNameGenericClassExtension() throws Exception {
        TypeDescription typeDescription = new TypeDescription.ForLoadedType(DuplicateNameGenericClass.Inner.class);
        MethodGraph.Linked methodGraph = MethodGraph.Compiler.Default.forJavaHierarchy().compile(typeDescription);
        assertThat(methodGraph.listNodes().size(), is(TypeDescription.OBJECT.getDeclaredMethods().filter(isVirtual()).size() + 4));
        MethodDescription objectMethod = typeDescription.getSuperType().getDeclaredMethods().filter(takesArguments(String.class)).getOnly();
        MethodGraph.Node objectNode = methodGraph.locate(objectMethod.asToken());
        assertThat(objectNode.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(objectNode.getRepresentative(), is(objectMethod));
        assertThat(objectNode.getBridges().size(), is(1));
        assertThat(objectNode.getBridges().contains(objectMethod.asDefined().asTypeToken()), is(true));
        assertThat(objectNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
        MethodDescription integerMethod = typeDescription.getSuperType().getDeclaredMethods().filter(takesArguments(Integer.class)).getOnly();
        MethodGraph.Node integerNode = methodGraph.locate(integerMethod.asToken());
        assertThat(integerNode.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(integerNode.getRepresentative(), is(integerMethod));
        assertThat(integerNode.getBridges().size(), is(0));
        assertThat(integerNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
        MethodDescription voidMethod = typeDescription.getDeclaredMethods().filter(takesArguments(Void.class)).getOnly();
        MethodGraph.Node voidNode = methodGraph.locate(voidMethod.asToken());
        assertThat(voidNode.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(voidNode.getRepresentative(), is(voidMethod));
        assertThat(voidNode.getBridges().size(), is(0));
        assertThat(voidNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
    }

    @Test
    public void testDuplicateNameGenericInterface() throws Exception {
        TypeDescription typeDescription = new TypeDescription.ForLoadedType(DuplicateNameGenericInterface.class);
        MethodGraph.Linked methodGraph = MethodGraph.Compiler.Default.forJavaHierarchy().compile(typeDescription);
        assertThat(methodGraph.listNodes().size(), is(2));
        MethodDescription objectMethod = typeDescription.getDeclaredMethods().filter(takesArguments(Object.class)).getOnly();
        MethodGraph.Node objectNode = methodGraph.locate(objectMethod.asToken());
        assertThat(objectNode.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(objectNode.getRepresentative(), is(objectMethod));
        assertThat(objectNode.getBridges().size(), is(0));
        assertThat(objectNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
        MethodDescription voidMethod = typeDescription.getDeclaredMethods().filter(takesArguments(Integer.class)).getOnly();
        MethodGraph.Node voidNode = methodGraph.locate(voidMethod.asToken());
        assertThat(voidNode.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(voidNode.getRepresentative(), is(voidMethod));
        assertThat(voidNode.getBridges().size(), is(0));
        assertThat(voidNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
    }

    @Test
    public void testDuplicateNameGenericInterfaceImplementation() throws Exception {
        TypeDescription typeDescription = new TypeDescription.ForLoadedType(DuplicateNameGenericInterface.InnerClass.class);
        MethodGraph.Linked methodGraph = MethodGraph.Compiler.Default.forJavaHierarchy().compile(typeDescription);
        assertThat(methodGraph.listNodes().size(), is(TypeDescription.OBJECT.getDeclaredMethods().filter(isVirtual()).size() + 4));
        MethodDescription objectMethod = typeDescription.getInterfaces().getOnly().getDeclaredMethods().filter(takesArguments(String.class)).getOnly();
        MethodGraph.Node objectNode = methodGraph.locate(objectMethod.asToken());
        assertThat(objectNode.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(objectNode.getRepresentative(), is(objectMethod));
        assertThat(objectNode.getBridges().size(), is(1));
        assertThat(objectNode.getBridges().contains(objectMethod.asDefined().asTypeToken()), is(true));
        assertThat(objectNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
        MethodDescription integerMethod = typeDescription.getInterfaces().getOnly().getDeclaredMethods().filter(takesArguments(Integer.class)).getOnly();
        MethodGraph.Node integerNode = methodGraph.locate(integerMethod.asToken());
        assertThat(integerNode.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(integerNode.getRepresentative(), is(integerMethod));
        assertThat(integerNode.getBridges().size(), is(0));
        assertThat(integerNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
        MethodDescription voidMethod = typeDescription.getDeclaredMethods().filter(takesArguments(Void.class)).getOnly();
        MethodGraph.Node voidNode = methodGraph.locate(voidMethod.asToken());
        assertThat(voidNode.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(voidNode.getRepresentative(), is(voidMethod));
        assertThat(voidNode.getBridges().size(), is(0));
        assertThat(voidNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
    }

    @Test
    public void testDuplicateNameGenericInterfaceExtension() throws Exception {
        TypeDescription typeDescription = new TypeDescription.ForLoadedType(DuplicateNameGenericInterface.InnerInterface.class);
        MethodGraph.Linked methodGraph = MethodGraph.Compiler.Default.forJavaHierarchy().compile(typeDescription);
        assertThat(methodGraph.listNodes().size(), is(3));
        MethodDescription objectMethod = typeDescription.getInterfaces().getOnly().getDeclaredMethods().filter(takesArguments(String.class)).getOnly();
        MethodGraph.Node objectNode = methodGraph.locate(objectMethod.asToken());
        assertThat(objectNode.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(objectNode.getRepresentative(), is(objectMethod));
        assertThat(objectNode.getBridges().size(), is(1));
        assertThat(objectNode.getBridges().contains(objectMethod.asDefined().asTypeToken()), is(true));
        assertThat(objectNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
        MethodDescription integerMethod = typeDescription.getInterfaces().getOnly().getDeclaredMethods().filter(takesArguments(Integer.class)).getOnly();
        MethodGraph.Node integerNode = methodGraph.locate(integerMethod.asToken());
        assertThat(integerNode.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(integerNode.getRepresentative(), is(integerMethod));
        assertThat(integerNode.getBridges().size(), is(0));
        assertThat(integerNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
        MethodDescription voidMethod = typeDescription.getDeclaredMethods().filter(takesArguments(Void.class)).getOnly();
        MethodGraph.Node voidNode = methodGraph.locate(voidMethod.asToken());
        assertThat(voidNode.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(voidNode.getRepresentative(), is(voidMethod));
        assertThat(voidNode.getBridges().size(), is(0));
        assertThat(voidNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
    }

    @Test
    public void testVisibilityBridge() throws Exception {
        TypeDescription typeDescription = new TypeDescription.ForLoadedType(VisibilityBridgeTarget.class);
        MethodGraph.Linked methodGraph = MethodGraph.Compiler.Default.forJavaHierarchy().compile(typeDescription);
        assertThat(methodGraph.listNodes().size(), is(TypeDescription.OBJECT.getDeclaredMethods().filter(isVirtual()).size() + 2));
        MethodDescription methodDescription = typeDescription.getSuperType().getDeclaredMethods().filter(isMethod()).getOnly();
        MethodGraph.Node methodNode = methodGraph.locate(methodDescription.asToken());
        assertThat(methodNode.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(methodNode.getVisibility(), is(MethodGraph.Node.Visibility.BRIDGED));
        assertThat(methodNode.getBridges().size(), is(0));
        assertThat(methodNode.getRepresentative(), is(methodDescription));
    }

    @Test
    public void testGenericVisibilityBridge() throws Exception {
        TypeDescription typeDescription = new TypeDescription.ForLoadedType(GenericVisibilityBridgeTarget.class);
        MethodGraph.Linked methodGraph = MethodGraph.Compiler.Default.forJavaHierarchy().compile(typeDescription);
        assertThat(methodGraph.listNodes().size(), is(TypeDescription.OBJECT.getDeclaredMethods().filter(isVirtual()).size() + 2));
        MethodDescription methodDescription = typeDescription.getSuperType()
                .getDeclaredMethods().filter(isMethod().and(ElementMatchers.not(isBridge()))).getOnly();
        MethodDescription.Token bridgeToken = typeDescription.getSuperType().getSuperType()
                .getDeclaredMethods().filter(isMethod()).getOnly().asToken();
        MethodGraph.Node methodNode = methodGraph.locate(methodDescription.asToken());
        assertThat(methodNode.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(methodNode, is(methodGraph.locate(bridgeToken)));
        assertThat(methodNode.getVisibility(), is(MethodGraph.Node.Visibility.BRIDGED));
        assertThat(methodNode.getBridges().size(), is(1));
        assertThat(methodNode.getRepresentative(), is(methodDescription));
    }

    @Test
    public void testMethodClassConvergence() throws Exception {
        TypeDescription typeDescription = new TypeDescription.ForLoadedType(MethodClassConvergence.Inner.class);
        MethodGraph.Linked methodGraph = MethodGraph.Compiler.Default.forJavaHierarchy().compile(typeDescription);
        assertThat(methodGraph.listNodes().size(), is(TypeDescription.OBJECT.getDeclaredMethods().filter(isVirtual()).size() + 2));
        MethodDescription methodDescription = typeDescription.getDeclaredMethods().filter(isMethod().and(ElementMatchers.not(isBridge()))).getOnly();
        MethodDescription genericMethod = typeDescription.getSuperType().getDeclaredMethods()
                .filter(isMethod().and(definedMethod(takesArguments(Object.class)))).getOnly();
        MethodDescription nonGenericMethod = typeDescription.getSuperType().getDeclaredMethods()
                .filter(isMethod().and(definedMethod(takesArguments(Void.class)))).getOnly();
        MethodGraph.Node methodNode = methodGraph.locate(methodDescription.asToken());
        assertThat(methodNode.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(methodNode, is(methodGraph.locate(genericMethod.asDefined().asToken())));
        assertThat(methodNode, is(methodGraph.locate(nonGenericMethod.asDefined().asToken())));
        assertThat(methodNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
        assertThat(methodNode.getBridges().size(), is(1));
        assertThat(methodNode.getRepresentative(), is(methodDescription));
        MethodGraph superGraph = methodGraph.getSuperGraph();
        MethodGraph.Node superNode = superGraph.locate(methodDescription.asToken());
        assertThat(superNode.getSort(), is(MethodGraph.Node.Sort.AMBIGUOUS));
        assertThat(superNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
        assertThat(superNode.getBridges().size(), is(1));
        assertThat(superNode.getRepresentative(), is(nonGenericMethod));
        assertThat(superNode.getRepresentative(), is(genericMethod));
    }

    @Test
    public void testMethodInterfaceConvergence() throws Exception {
        TypeDescription typeDescription = new TypeDescription.ForLoadedType(MethodInterfaceConvergenceTarget.class);
        MethodGraph.Linked methodGraph = MethodGraph.Compiler.Default.forJavaHierarchy().compile(typeDescription);
        assertThat(methodGraph.listNodes().size(), is(1));
        MethodDescription genericMethod = typeDescription.getInterfaces().filter(rawType(MethodInterfaceConvergenceFirstBase.class)).getOnly()
                .getDeclaredMethods().filter(isMethod()).getOnly();
        MethodDescription nonGenericMethod = typeDescription.getInterfaces().filter(rawType(MethodInterfaceConvergenceSecondBase.class)).getOnly()
                .getDeclaredMethods().filter(isMethod()).getOnly();
        assertThat(methodGraph.getSuperGraph().locate(genericMethod.asToken()).getSort(), is(MethodGraph.Node.Sort.UNRESOLVED));
        assertThat(methodGraph.getSuperGraph().locate(nonGenericMethod.asToken()).getSort(), is(MethodGraph.Node.Sort.UNRESOLVED));
        MethodGraph.Node methodNode = methodGraph.locate(genericMethod.asToken());
        assertThat(methodNode.getSort(), is(MethodGraph.Node.Sort.AMBIGUOUS));
        assertThat(methodNode, is(methodGraph.locate(genericMethod.asDefined().asToken())));
        assertThat(methodNode, is(methodGraph.locate(nonGenericMethod.asDefined().asToken())));
        assertThat(methodNode.getVisibility(), is(MethodGraph.Node.Visibility.PLAIN));
        assertThat(methodNode.getBridges().size(), is(1));
        assertThat(methodNode.getBridges().contains(genericMethod.asDefined().asTypeToken()), is(true));
        assertThat(methodNode.getRepresentative(), is(genericMethod));
        assertThat(methodNode.getRepresentative(), not(nonGenericMethod));
    }

    @Test
    public void testMethodConvergenceVisibilityTarget() throws Exception {
        TypeDescription typeDescription = new TypeDescription.ForLoadedType(MethodConvergenceVisibilityBridgeTarget.class);
        MethodGraph.Linked methodGraph = MethodGraph.Compiler.Default.forJavaHierarchy().compile(typeDescription);
        assertThat(methodGraph.listNodes().size(), is(TypeDescription.OBJECT.getDeclaredMethods().filter(isVirtual()).size() + 2));
        MethodDescription genericMethod = typeDescription.getSuperType().getSuperType()
                .getDeclaredMethods().filter(isMethod().and(definedMethod(takesArguments(Object.class)))).getOnly();
        MethodDescription nonGenericMethod = typeDescription.getSuperType().getSuperType()
                .getDeclaredMethods().filter(isMethod().and(definedMethod(takesArguments(Void.class)))).getOnly();
        MethodGraph.Node methodNode = methodGraph.locate(genericMethod.asToken());
        assertThat(methodNode.getSort(), is(MethodGraph.Node.Sort.RESOLVED));
        assertThat(methodNode, is(methodGraph.locate(nonGenericMethod.asToken())));
        assertThat(methodNode.getVisibility(), is(MethodGraph.Node.Visibility.BRIDGED));
        assertThat(methodNode.getBridges().size(), is(1));
        assertThat(methodNode.getBridges().contains(genericMethod.asDefined().asTypeToken()), is(true));
        assertThat(methodNode.getRepresentative(), is(typeDescription.getSuperType().getDeclaredMethods()
                .filter(isMethod().and(ElementMatchers.not(isBridge()))).getOnly()));
    }

    @Test
    public void testObjectProperties() throws Exception {
        ObjectPropertyAssertion.of(MethodGraph.Compiler.Default.class).apply();
    }

    public static class SimpleClass {
        /* empty */
    }

    public interface SimpleInterface {
        /* empty */
    }

    public static class ClassBase {

        public void foo() {
            /* empty */
        }

        static class Inner extends ClassBase {

            @Override
            public void foo() {
                /* empty */
            }
        }
    }

    public interface InterfaceBase {

        void foo();

        abstract class InnerClass implements InterfaceBase {
            /* empty */
        }

        interface InnerInterface extends InterfaceBase {
            /* empty */
        }
    }

    public static class ClassAndInterfaceInheritance extends ClassBase implements InterfaceBase {
        /* empty */
    }

    public interface AmbiguousInterfaceBase {

        void foo();

        interface InterfaceTarget extends InterfaceBase, AmbiguousInterfaceBase {
            /* empty */
        }

        abstract class ClassTarget implements InterfaceBase, AmbiguousInterfaceBase {
            /* empty */
        }

        interface DominantIntermediate extends InterfaceBase, AmbiguousInterfaceBase {

            @Override
            void foo();
        }

        abstract class DominantClassBase implements DominantIntermediate {
            /* empty */
        }

        abstract class DominantClassTarget extends DominantClassBase implements InterfaceBase {
            /* empty */
        }

        interface DominantInterfaceTargetLeft extends InterfaceBase, DominantIntermediate {
            /* empty */
        }

        interface DominantInterfaceTargetRight extends DominantIntermediate, InterfaceBase {
            /* empty */
        }

        interface NonDominantAmbiguous {

            void foo();
        }

        interface NonDominantIntermediateLeft extends InterfaceBase, NonDominantAmbiguous {
            /* empty */
        }

        interface NonDominantIntermediateRight extends NonDominantAmbiguous,InterfaceBase {
            /* empty */
        }

        interface NonDominantTargetLeft extends DominantIntermediate, NonDominantIntermediateLeft {
            /* empty */
        }

        interface NonDominantTargetRight extends DominantIntermediate, NonDominantIntermediateRight {
            /* empty */
        }
    }

    public static class GenericClassBase<T> {

        public void foo(T t) {
            /* empty */
        }

        public static class Inner extends GenericClassBase<Void> {

            @Override
            public void foo(Void t) {
                /* empty */
            }
        }

        public static class Intermediate<T extends Number> extends GenericClassBase<T> {

            @Override
            public void foo(T t) {
                /* empty */
            }

            public static class Inner extends Intermediate<Integer> {

                @Override
                public void foo(Integer t) {
                    /* empty */
                }
            }
        }
    }

    public static class ReturnTypeClassBase {

        public Object foo() {
            return null;
        }

        public static class Inner extends ReturnTypeClassBase {

            @Override
            public Void foo() {
                return null;
            }
        }

        public static class Intermediate extends ReturnTypeClassBase {

            @Override
            public Number foo() {
                return null;
            }

            public static class Inner extends Intermediate {

                @Override
                public Integer foo() {
                    return null;
                }
            }
        }
    }

    public interface GenericInterfaceBase<T> {

        void foo(T t);

        interface Inner extends GenericInterfaceBase<Void> {

            @Override
            void foo(Void t);
        }

        interface Intermediate<T extends Number> extends GenericInterfaceBase<T> {

            @Override
            void foo(T t);

            interface Inner extends Intermediate<Integer> {

                @Override
                void foo(Integer t);
            }
        }
    }

    public interface ReturnTypeInterfaceBase {

        Object foo();

        interface Inner extends ReturnTypeInterfaceBase {

            @Override
            Void foo();
        }

        interface Intermediate extends ReturnTypeInterfaceBase {

            @Override
            Number foo();

            interface Inner extends Intermediate {

                @Override
                Integer foo();
            }
        }
    }

    public static class GenericWithReturnTypeClassBase<T> {

        public Object foo(T t) {
            return null;
        }

        public static class Inner extends GenericWithReturnTypeClassBase<Void> {

            @Override
            public Void foo(Void t) {
                return null;
            }
        }

        public static class Intermediate<T extends Number> extends GenericWithReturnTypeClassBase<T> {

            @Override
            public Number foo(T t) {
                return null;
            }

            public static class Inner extends Intermediate<Integer> {

                @Override
                public Integer foo(Integer t) {
                    return null;
                }
            }
        }
    }

    public interface GenericWithReturnTypeInterfaceBase<T> {

        Object foo(T t);

        interface Inner extends GenericWithReturnTypeInterfaceBase<Void> {

            @Override
            Void foo(Void t);
        }

        interface Intermediate<T extends Number> extends GenericWithReturnTypeInterfaceBase<T> {

            @Override
            Number foo(T t);

            interface Inner extends Intermediate<Integer> {

                @Override
                Integer foo(Integer t);
            }
        }
    }

    @SuppressWarnings("unused")
    public static class GenericNonOverriddenClassBase<T> {

        public T foo(T t) {
            return null;
        }

        public class Inner extends GenericNonOverriddenClassBase<Void> {
            /* empty */
        }
    }

    public interface GenericNonOverriddenInterfaceBase<T> {

        T foo(T t);

        interface InnerInterface extends GenericNonOverriddenInterfaceBase<Void> {
            /* empty */
        }

        abstract class InnerClass implements GenericNonOverriddenInterfaceBase<Void> {
            /* empty */
        }
    }

    @SuppressWarnings("unused")
    public static class DuplicateNameClass {

        public void foo(Object o) {
            /* empty */
        }

        public void foo(Integer o) {
            /* empty */
        }

        public static class Inner extends DuplicateNameClass {

            public void foo(Void o) {
                /* empty */
            }
        }
    }

    public interface DuplicateNameInterface {

        void foo(Object o);

        void foo(Integer o);

        interface InnerInterface extends DuplicateNameInterface {

            void foo(Void o);
        }

        abstract class InnerClass implements DuplicateNameInterface {

            public abstract void foo(Void o);
        }
    }

    @SuppressWarnings("unused")
    public static class DuplicateNameGenericClass<T> {

        public void foo(T o) {
            /* empty */
        }

        public void foo(Integer o) {
            /* empty */
        }

        public static class Inner extends DuplicateNameGenericClass<String> {

            public void foo(Void o) {
                /* empty */
            }
        }
    }

    public interface DuplicateNameGenericInterface<T> {

        void foo(T o);

        void foo(Integer o);

        interface InnerInterface extends DuplicateNameGenericInterface<String> {

            void foo(Void o);
        }

        @SuppressWarnings("unused")
        abstract class InnerClass implements DuplicateNameGenericInterface<String> {

            public abstract void foo(Void o);
        }
    }

    static class VisibilityBridgeBase {

        public void foo() {
            /* empty */
        }
    }

    public static class VisibilityBridgeTarget extends VisibilityBridgeBase {
        /* empty */
    }

    public static class GenericVisibilityBridgeBase<T> {

        public void foo(T t) {
            /* empty */
        }
    }

    static class GenericVisibilityBridge extends GenericVisibilityBridgeBase<Void> {

        @Override
        public void foo(Void aVoid) {
            /* empty */
        }
    }

    public static class GenericVisibilityBridgeTarget extends GenericVisibilityBridge {
        /* empty */
    }

    public static class MethodClassConvergence<T> {

        public T foo(T arg) {
            return null;
        }

        public Void foo(Void arg) {
            return null;
        }

        public static class Inner extends MethodClassConvergence<Void> {

            @Override
            public Void foo(Void arg) {
                return null;
            }
        }
    }

    public interface MethodInterfaceConvergenceFirstBase<T> {

        T foo();
    }

    public interface MethodInterfaceConvergenceSecondBase {

        Void foo();
    }

    public interface MethodInterfaceConvergenceTarget extends MethodInterfaceConvergenceFirstBase<Void>, MethodInterfaceConvergenceSecondBase {
        /* empty */
    }

    static class MethodConvergenceVisibilityBridgeBase<T> {

        public T foo(T arg) {
            return null;
        }

        public Void foo(Void arg) {
            return null;
        }
    }

    static class MethodConvergenceVisibilityBridgeIntermediate extends MethodConvergenceVisibilityBridgeBase<Void> {

        @Override
        public Void foo(Void arg) {
            return null;
        }
    }

    public static class MethodConvergenceVisibilityBridgeTarget extends MethodConvergenceVisibilityBridgeIntermediate {
        /* empty */
    }
}
