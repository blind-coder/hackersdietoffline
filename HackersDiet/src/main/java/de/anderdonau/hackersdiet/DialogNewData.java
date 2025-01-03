package de.anderdonau.hackersdiet;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import java.util.Date;

public class DialogNewData extends Dialog
  implements View.OnClickListener
{
  public Activity c;
  public Button no;
  private final MonthDetailFragment parent;
  private final weightData weight;
  public Button yes;

  public DialogNewData(Activity paramActivity, weightData paramweightData, MonthDetailFragment paramMonthDetailFragment)
  {
    super(paramActivity);
    this.weight = paramweightData;
    this.parent = paramMonthDetailFragment;
    this.c = paramActivity;
  }

  public void onClick(View view)
  {
    if (view.getId() == R.id.dNDBtnAdd)
    {
      DatePicker datePicker = findViewById(R.id.dialogNewDataDatePicker);
      EditText editWeight = findViewById(R.id.dNDEditWeight);
      EditText editComment = findViewById(R.id.dNDEditComment);
      CheckBox chkFlag = findViewById(R.id.dNDCheckFlag);
      EditText editRung = findViewById(R.id.dNDEditRung);
      String strRung = editRung.getText().toString();
      int i = strRung.length();
      String rung;
      if (i == 0) {
        rung = "0";
      } else {
        rung = editRung.getText().toString();
      }
      this.weight.add(datePicker.getYear(), datePicker.getMonth() + 1,
              datePicker.getDayOfMonth(), Double.parseDouble(editWeight.getText().toString()),
              Integer.parseInt(rung), chkFlag.isChecked(),
              editComment.getText().toString());
      this.weight.saveData();
      this.parent.updateEverything();
    }
    dismiss();
  }

  protected void onCreate(Bundle bundle)
  {
    super.onCreate(bundle);
    requestWindowFeature(1);
    setContentView(R.layout.dialog_new_data);
    this.yes = (findViewById(R.id.dNDBtnAdd));
    this.no = (findViewById(R.id.dNDBtnCancel));
    this.yes.setOnClickListener(this);
    this.no.setOnClickListener(this);
    ((DatePicker)findViewById(R.id.dialogNewDataDatePicker)).setMaxDate(new Date().getTime());
  }
}