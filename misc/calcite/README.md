```java
public Object[] apply(Object root0) {
  int case_when_value;
  final Integer not_null_udf_value = com.example.ExprEvaluation.Udf1.calculate_score(org.apache.calcite.runtime.SqlFunctions.toInt(((Object[]) ((org.apache.calcite.DataContext) root0).get("inputRecord"))[0]), org.apache.calcite.runtime.SqlFunctions.toInt(((Object[]) ((org.apache.calcite.DataContext) root0).get("inputRecord"))[1]), org.apache.calcite.runtime.SqlFunctions.toInt(((Object[]) ((org.apache.calcite.DataContext) root0).get("inputRecord"))[2]));
  final Boolean binary_call_value = not_null_udf_value == null ? null : Boolean.valueOf(not_null_udf_value.intValue() > 20);
  if (binary_call_value != null && binary_call_value) {
    case_when_value = 1;
  } else {
    final Integer not_null_udf_value0 = com.example.ExprEvaluation.Udf2.calculate_score(org.apache.calcite.runtime.SqlFunctions.toInt(((Object[]) ((org.apache.calcite.DataContext) root0).get("inputRecord"))[0]), org.apache.calcite.runtime.SqlFunctions.toInt(((Object[]) ((org.apache.calcite.DataContext) root0).get("inputRecord"))[1]), org.apache.calcite.runtime.SqlFunctions.toInt(((Object[]) ((org.apache.calcite.DataContext) root0).get("inputRecord"))[2]));
    final Boolean binary_call_value0 = not_null_udf_value0 == null ? null : Boolean.valueOf(not_null_udf_value0.intValue() > 10);
    if (binary_call_value0 != null && binary_call_value0) {
      case_when_value = 2;
    } else {
      case_when_value = 3;
    }
  }
  return new Object[] {
      case_when_value};
}
```