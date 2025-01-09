

package MiniVHDL;
import java.util.ArrayList;
import java.util.List;
import MiniVHDL.Circuit.*;
import MiniVHDL.Circuit.Expression.*;
import MiniVHDL.Circuit.Wire.*;



public class Parser {
    public static final int _EOF = 0;
    public static final int _ident = 1;
    public static final int _number = 2;
    public static final int _colon = 3;
    public static final int maxT = 40;

    static final boolean _T = true;
    static final boolean _x = false;
    static final int minErrDist = 2;

    public Token t;    // last recognized token
    public Token la;   // lookahead token
    int errDist = minErrDist;

    public Scanner scanner;
    public Errors errors;

    Circuit circuit;



    public Parser(Scanner scanner) {
        this.scanner = scanner;
        errors = new Errors();
    }

    void SynErr (int n) {
        if (errDist >= minErrDist) errors.SynErr(la.line, la.col, n);
        errDist = 0;
    }

    public void SemErr (String msg) {
        if (errDist >= minErrDist) errors.SemErr(t.line, t.col, msg);
        errDist = 0;
    }

    void Get () {
        for (;;) {
            t = la;
            la = scanner.Scan();
            if (la.kind <= maxT) {
                ++errDist;
                break;
            }

            la = t;
        }
    }

    void Expect (int n) {
        if (la.kind==n) Get(); else { SynErr(n); }
    }

    boolean StartOf (int s) {
        return set[s][la.kind];
    }

    void ExpectWeak (int n, int follow) {
        if (la.kind == n) Get();
        else {
            SynErr(n);
            while (!StartOf(follow)) Get();
        }
    }

    boolean WeakSeparator (int n, int syFol, int repFol) {
        int kind = la.kind;
        if (kind == n) { Get(); return true; }
        else if (StartOf(repFol)) return false;
        else {
            SynErr(n);
            while (!(set[syFol][kind] || set[repFol][kind] || set[0][kind])) {
                Get();
                kind = la.kind;
            }
            return StartOf(syFol);
        }
    }

    void MiniVHDL() {
        circuit=new Circuit(); Entity entity;
        while (StartOf(1)) {
            if (la.kind == 4) {
                LibraryClause();
            } else if (la.kind == 7) {
                UseClause();
            } else if (la.kind == 9) {
                entity = EntityDecl();
                circuit.entities.add(entity);
            } else {
                ArchitectureDecl(circuit);
            }
        }
        String undriven= circuit.getUndrivenWires();
        if(!undriven.isEmpty()){
            SemErr("The following wires are not driven:\n%s".formatted(undriven));
        }
    }

    void LibraryClause() {
        Expect(4);
        Expect(5);
        Expect(6);
    }

    void UseClause() {
        Expect(7);
        Expect(8);
        Expect(6);
    }

    Entity  EntityDecl() {
        Entity  entity;
        List<Port> ports;
        Expect(9);
        Expect(1);
        String name = t.val.toLowerCase();
        Expect(10);
        Expect(11);
        Expect(12);
        ports = PortList();
        Expect(13);
        Expect(6);
        Expect(14);
        if (la.kind == 9) {
            Get();
        }
        if (la.kind == 1) {
            Get();
            if(!t.val.toLowerCase().equals(name)){
                SemErr("Name not matching declaration");
            }
        }
        Expect(6);
        entity=new Entity(name, ports);
        return entity;
    }

    void ArchitectureDecl(Circuit circuit) {
        Component component; List<Signal> signals;
        Expect(21);
        Expect(1);
        String architectureName = t.val.toLowerCase();
        Architecture architecture = new Architecture(architectureName);
        Expect(22);
        Expect(1);
        String entityName = t.val.toLowerCase();
        Entity entity = circuit.getEntityByName(entityName);
        if (entity == null) {
            SemErr("referenced entity does not exist");
        } else {
            entity.architecture = architecture;
            architecture.entity = entity;
        }
        Expect(10);
        while (la.kind == 24) {
            component = ComponentDecl(circuit);
            architecture.components.add(component);
        }
        while (la.kind == 25) {
            signals = SignalDecl();
            architecture.signals.addAll(signals);
        }
        Expect(23);
        while (la.kind == 1 || la.kind == 37 || la.kind == 38) {
            ConcurrentStmt(architecture);
        }
        Expect(14);
        if (la.kind == 21) {
            Get();
        }
        if (la.kind == 1) {
            Get();
            if(!t.val.toLowerCase().equals(architectureName)){
                SemErr("Name not matching declaration");
            }
        }
        Expect(6);
    }

    List<Port>  PortList() {
        List<Port>  ports;
        List<Port> ports2;
        ports = PortDecl();
        while (la.kind == 6) {
            Get();
            ports2 = PortDecl();
            ports.addAll(ports2);
        }
        return ports;
    }

    List<Port>  PortDecl() {
        List<Port>  ports;
        ports = new ArrayList<>(); Port.Direction dir; List<String> idents; int[] length_offset;
        idents = identList();
        Expect(3);
        dir = Direction();
        length_offset = SubtypeInd();
        for (String ident : idents) {
            ports.add(new Port(ident, dir, length_offset[0], length_offset[1]));
        }
        return ports;
    }

    List<String>  identList() {
        List<String>  idents;
        idents = new ArrayList<>();
        Expect(1);
        idents.add(t.val.toLowerCase());
        while (la.kind == 15) {
            Get();
            Expect(1);
            idents.add(t.val.toLowerCase());
        }
        return idents;
    }

    Port.Direction  Direction() {
        Port.Direction  dir;
        dir = null;
        if (la.kind == 16) {
            Get();
            dir = Port.Direction.IN;
        } else if (la.kind == 17) {
            Get();
            dir = Port.Direction.OUT;
        } else SynErr(41);
        return dir;
    }

    int[]  SubtypeInd() {
        int[]  length_offset;
        length_offset = new int[2];
        if (la.kind == 18) {
            Get();
            length_offset[0] = 1; length_offset[1] = 0;
        } else if (la.kind == 19) {
            length_offset = StdLogicVectorType();
        } else SynErr(42);
        return length_offset;
    }

    int[]  StdLogicVectorType() {
        int[]  length_offset;
        length_offset = new int[2];
        Expect(19);
        Expect(12);
        Expect(2);
        int start = Integer.parseInt(t.val);
        Expect(20);
        Expect(2);
        int end = Integer.parseInt(t.val);
        Expect(13);
        length_offset[0] = start - end + 1;
        length_offset[1] = end;
        return length_offset;
    }

    Component  ComponentDecl(Circuit circuit) {
        Component  component;
        List<Port> ports;
        Expect(24);
        Expect(1);
        String name = t.val.toLowerCase();
        Entity componentEntity = circuit.getEntityByName(name);
        if(componentEntity==null){
            SemErr("There is no matching entity to this component");
        }
        if (la.kind == 10) {
            Get();
        }
        Expect(11);
        Expect(12);
        ports = PortList();
        Expect(13);
        Expect(6);
        if(componentEntity!=null){
            List<Port> onlyInEntity = componentEntity.ports.stream()
                    .filter(port -> !ports.contains(port))
                    .toList();
            List<Port> onlyInComponent = ports.stream()
                    .filter(port -> !componentEntity.ports.contains(port))
                    .toList();
            if (!onlyInEntity.isEmpty()) {
                onlyInEntity.forEach(port -> SemErr("Port \"" + port.name+"\" of referenced entity is not defined equal in component"));
            }
            if (!onlyInComponent.isEmpty()) {
                onlyInComponent.forEach(port -> SemErr("Port \"" + port.name+"\" is not defined equal in referenced entity"));
            }
        }
        Expect(14);
        Expect(24);
        if (la.kind == 1) {
            Get();
            if(!t.val.toLowerCase().equals(name)){
                SemErr("Name not matching declaration");
            }
        }
        Expect(6);
        component=new Component(name, ports);
        return component;
    }

    List<Signal>  SignalDecl() {
        List<Signal>  signals;
        List<String> idents; int[] length_offset;
        Expect(25);
        idents = identList();
        Expect(3);
        length_offset = SubtypeInd();
        Expect(6);
        signals = new ArrayList<>();
        for (String ident : idents) {
            signals.add(new Signal(ident, length_offset[0], length_offset[1]));
        }
        return signals;
    }

    void ConcurrentStmt(Architecture architecture) {
        Token la2 = scanner.Peek();
        if (la2.kind == _colon) {
            InstantiationStmt(architecture);
        } else if (la.kind == 1 || la.kind == 37 || la.kind == 38) {
            AssignmentStmt(architecture);
        } else SynErr(43);
    }

    void InstantiationStmt(Architecture architecture) {
        Expect(1);
        String instanceName = t.val.toLowerCase();
        Expect(3);
        Expect(1);
        String componentName = t.val.toLowerCase();
        Component component = architecture.getComponentFromIdent(componentName);
        if (component == null) {
            SemErr("component not defined");
            component=new Component("", new ArrayList<>());
        }
        Instance instance = new Instance(instanceName, component);
        architecture.instances.add(instance);
        Expect(11);
        Expect(36);
        Expect(12);
        PortMap(architecture, instance);
        Expect(13);
        Expect(6);
    }

    void AssignmentStmt(Architecture architecture) {
        Expression to; Expression from;
        to = Indexedident(architecture);
        Expect(26);
        from = Expression(architecture);
        if (from!=null && to!=null &&from.width != to.width) {
            SemErr("assignment operands are not of equal width");
        }
        Expect(6);
        if(to!=null&&!to.isAssignable()){
            SemErr("Cannot assign to expression");
        }
        if(to!=null&&architecture.isAlreadyDriven(to)){
            SemErr("Assignment destination is already driven");
        }
        architecture.connections.add(new Connection(from, to));
    }

    Expression  Indexedident(Architecture architecture) {
        Expression  expr;
        expr = null;
        if (la.kind == 1) {
            Get();
            expr = architecture.getWireFromIdent(t.val.toLowerCase());
            if(expr==null){
                SemErr("port or signal not defined");
            }
            if (la.kind == 12) {
                Get();
                Expect(2);
                int start = Integer.parseInt(t.val);
                Integer end = null;
                if (la.kind == 20) {
                    Get();
                    Expect(2);
                    end = Integer.parseInt(t.val);
                }
                if (expr != null) {
                    if (end == null) {
                        int offset=start - expr.getIndexOffset();
                        if(offset>expr.width||offset<0){
                            SemErr("Index out of bounds");
                        }
                        expr = new WidthExpression(1, expr, offset);
                    } else {
                        if(start<end){
                            SemErr("DOWNTO start must be larger than end");
                        }
                        int offset=end - expr.getIndexOffset();
                        if(offset>expr.width||offset<0){
                            SemErr("Index out of bounds");
                        }
                        expr = new WidthExpression(start - end + 1, expr, offset);
                    }
                }
                Expect(13);
            }
        } else if (la.kind == 37 || la.kind == 38) {
            expr = Immediate();
        } else SynErr(44);
        return expr;
    }

    Expression  Expression(Architecture architecture) {
        Expression  expr;
        OperationExpression.Operation op=null; Expression right;
        expr = SimpleTerm(architecture);
        while (StartOf(2)) {
            switch (la.kind) {
                case 27: {
                    Get();
                    op=OperationExpression.Operation.XOR;
                    break;
                }
                case 28: {
                    Get();
                    op=OperationExpression.Operation.AND;
                    break;
                }
                case 29: {
                    Get();
                    op=OperationExpression.Operation.OR;
                    break;
                }
                case 30: {
                    Get();
                    op=OperationExpression.Operation.NAND;
                    break;
                }
                case 31: {
                    Get();
                    op=OperationExpression.Operation.NOR;
                    break;
                }
                case 32: {
                    Get();
                    op=OperationExpression.Operation.XNOR;
                    break;
                }
                case 33: {
                    Get();
                    op=OperationExpression.Operation.CAT;
                    break;
                }
            }
            right = SimpleTerm(architecture);
            if (op!=OperationExpression.Operation.CAT && expr.width != right.width) {
                SemErr("operands are not of equal width");
            }
            expr = new OperationExpression(op!=OperationExpression.Operation.CAT?expr.width:expr.width+right.width, expr, right, op);
        }
        return expr;
    }

    Expression  SimpleTerm(Architecture architecture) {
        Expression  expr;
        expr=null; boolean negate=false;
        if (la.kind == 34) {
            Get();
            negate=true;
        }
        if (la.kind == 1 || la.kind == 37 || la.kind == 38) {
            expr = Indexedident(architecture);
        } else if (la.kind == 2) {
            Get();
            int start = Integer.parseInt(t.val);
            Expect(20);
            Expect(2);
            int end = Integer.parseInt(t.val);
            Expect(35);
            Expect(1);
            String ident = t.val.toLowerCase();
            Expression source = architecture.getWireFromIdent(ident);
            if (source == null) {
                SemErr("port or signal not defined");
            }
            if (la.kind == 12) {
                Get();
                Expect(2);
                int index = Integer.parseInt(t.val);
                Expect(13);
                if(source!=null){
                    int offset = index - source.getIndexOffset();
                    if(offset>source.width||offset<0){
                        SemErr("Index out of bounds");
                    }
                    source = new WidthExpression(1, source, offset);
                }
            }
            if (source != null && source.width != 1) {
                SemErr("width expansion source is not of length 1");
            }
            expr = new WidthExpression(start - end + 1, source, end);
        } else if (la.kind == 12) {
            Get();
            expr = Expression(architecture);
            Expect(13);
        } else SynErr(45);
        if(negate&&expr!=null) {
            expr=new NegationExpression(expr.width,expr);
        }
        return expr;
    }

    void PortMap(Architecture architecture, Instance instance) {
        Expression expr = null; int i=0;
        if (StartOf(3)) {
            expr = Expression(architecture);
        } else if (la.kind == 39) {
            Get();
            expr = new PortWire(0,null);
            if(instance.component.ports.get(i).direction==Port.Direction.IN){
                SemErr("Cannot use OPEN for input");
            }
        } else SynErr(46);
        if (i >= instance.component.ports.size()) {
            SemErr("port map does not match number of ports");
        } else {
            if(instance.component.ports.get(i).direction==Port.Direction.OUT && !expr.isAssignable()){
                SemErr("Cannot assign to expression");
            }
            architecture.addPortConnection(instance, expr, i);
        }
        while (la.kind == 15) {
            Get();
            i++;
            if (StartOf(3)) {
                expr = Expression(architecture);
            } else if (la.kind == 39) {
                Get();
                expr = new PortWire(0,null);
                if(instance.component.ports.get(i).direction==Port.Direction.IN){
                    SemErr("Cannot use OPEN for input");
                }
            } else SynErr(47);
            if (i > instance.component.ports.size()) {
                SemErr("port map does not match number of ports");
            } else {
                if(instance.component.ports.get(i).direction==Port.Direction.OUT && !expr.isAssignable()){
                    SemErr("Cannot assign to expression");
                }
                architecture.addPortConnection(instance, expr, i);
            }
        }
        if (i != instance.component.ports.size() - 1) {
            SemErr("port map does not match number of ports");
        }
    }

    Expression  Immediate() {
        Expression  expr;
        List<Boolean> values=new ArrayList<>();
        if (la.kind == 37) {
            Get();
        } else if (la.kind == 38) {
            Get();
        } else SynErr(48);
        Expect(2);
        for(int i=0;i<t.val.length();i++){
            switch (t.val.charAt(i)) {
                case '0' -> values.add(false);
                case '1' -> values.add(true);
                default -> SemErr("Only values 0 or 1 allowed");
            }
        }
        if (la.kind == 37) {
            Get();
        } else if (la.kind == 38) {
            Get();
        } else SynErr(49);
        expr = new ImmediateWire(values.size(),values);
        return expr;
    }



    public void Parse() {
        la = new Token();
        la.val = "";
        Get();
        MiniVHDL();
        Expect(0);

        scanner.buffer.Close();
    }

    private static final boolean[][] set = {
            {_T,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x},
            {_x,_x,_x,_x, _T,_x,_x,_T, _x,_T,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_T,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x},
            {_x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_T, _T,_T,_T,_T, _T,_T,_x,_x, _x,_x,_x,_x, _x,_x},
            {_x,_T,_T,_x, _x,_x,_x,_x, _x,_x,_x,_x, _T,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_T,_x, _x,_T,_T,_x, _x,_x}

    };
} // end Parser


class Errors {
    public int count = 0;                                    // number of errors detected
    public java.io.PrintStream errorStream = System.out;     // error messages go to this stream
    public String errMsgFormat = "-- line {0} col {1}: {2}"; // 0=line, 1=column, 2=text

    protected void printMsg(int line, int column, String msg) {
        StringBuffer b = new StringBuffer(errMsgFormat);
        int pos = b.indexOf("{0}");
        if (pos >= 0) { b.delete(pos, pos+3); b.insert(pos, line); }
        pos = b.indexOf("{1}");
        if (pos >= 0) { b.delete(pos, pos+3); b.insert(pos, column); }
        pos = b.indexOf("{2}");
        if (pos >= 0) b.replace(pos, pos+3, msg);
        errorStream.println(b.toString());
    }

    public void SynErr (int line, int col, int n) {
        String s;
        switch (n) {
            case 0: s = "EOF expected"; break;
            case 1: s = "ident expected"; break;
            case 2: s = "number expected"; break;
            case 3: s = "colon expected"; break;
            case 4: s = "\"library\" expected"; break;
            case 5: s = "\"ieee\" expected"; break;
            case 6: s = "\";\" expected"; break;
            case 7: s = "\"use\" expected"; break;
            case 8: s = "\"ieee.std_logic_1164.all\" expected"; break;
            case 9: s = "\"entity\" expected"; break;
            case 10: s = "\"is\" expected"; break;
            case 11: s = "\"port\" expected"; break;
            case 12: s = "\"(\" expected"; break;
            case 13: s = "\")\" expected"; break;
            case 14: s = "\"end\" expected"; break;
            case 15: s = "\",\" expected"; break;
            case 16: s = "\"in\" expected"; break;
            case 17: s = "\"out\" expected"; break;
            case 18: s = "\"std_ulogic\" expected"; break;
            case 19: s = "\"std_ulogic_vector\" expected"; break;
            case 20: s = "\"downto\" expected"; break;
            case 21: s = "\"architecture\" expected"; break;
            case 22: s = "\"of\" expected"; break;
            case 23: s = "\"begin\" expected"; break;
            case 24: s = "\"component\" expected"; break;
            case 25: s = "\"signal\" expected"; break;
            case 26: s = "\"<=\" expected"; break;
            case 27: s = "\"xor\" expected"; break;
            case 28: s = "\"and\" expected"; break;
            case 29: s = "\"or\" expected"; break;
            case 30: s = "\"nand\" expected"; break;
            case 31: s = "\"nor\" expected"; break;
            case 32: s = "\"xnor\" expected"; break;
            case 33: s = "\"&\" expected"; break;
            case 34: s = "\"not\" expected"; break;
            case 35: s = "\"=>\" expected"; break;
            case 36: s = "\"map\" expected"; break;
            case 37: s = "\"\\\"\" expected"; break;
            case 38: s = "\"\'\" expected"; break;
            case 39: s = "\"open\" expected"; break;
            case 40: s = "??? expected"; break;
            case 41: s = "invalid Direction"; break;
            case 42: s = "invalid SubtypeInd"; break;
            case 43: s = "invalid ConcurrentStmt"; break;
            case 44: s = "invalid Indexedident"; break;
            case 45: s = "invalid SimpleTerm"; break;
            case 46: s = "invalid PortMap"; break;
            case 47: s = "invalid PortMap"; break;
            case 48: s = "invalid Immediate"; break;
            case 49: s = "invalid Immediate"; break;
            default: s = "error " + n; break;
        }
        printMsg(line, col, s);
        count++;
    }

    public void SemErr (int line, int col, String s) {
        printMsg(line, col, s);
        count++;
    }

    public void SemErr (String s) {
        errorStream.println(s);
        count++;
    }

    public void Warning (int line, int col, String s) {
        printMsg(line, col, s);
    }

    public void Warning (String s) {
        errorStream.println(s);
    }
} // Errors


class FatalError extends RuntimeException {
    public static final long serialVersionUID = 1L;
    public FatalError(String s) { super(s); }
}
