package de.anderdonau.hackersdiet;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DialogNewData extends Dialog
  implements View.OnClickListener
{
  public Activity c;
  public Button no;
  private final MonthDetailFragment parent;
  private final weightData weight;
  private final int mDay, mMonth, mYear;
  public Button yes;

  public DialogNewData(Activity paramActivity, weightData paramweightData, MonthDetailFragment paramMonthDetailFragment, int day, int month, int year)
  {
    super(paramActivity);
    this.weight = paramweightData;
    this.parent = paramMonthDetailFragment;
    this.c = paramActivity;
    this.mDay = day;
    this.mMonth = month;
    this.mYear = year;
  }

  public void onClick(View view)
  {
    if (view.getId() == R.id.dNDBtnAdd)
    {
//      DatePicker datePicker = findViewById(R.id.dialogNewDataDatePicker);
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
      this.weight.add(this.mYear, this.mMonth + 1, this.mDay,
              Double.parseDouble(editWeight.getText().toString()),
              Integer.parseInt(rung), chkFlag.isChecked(),
              editComment.getText().toString());
      this.weight.saveData(getContext());
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
    weightDataDay dataDay = this.weight.getByDate(this.mYear, this.mMonth +1, this.mDay);
    EditText editWeight = findViewById(R.id.dNDEditWeight);
    EditText editComment = findViewById(R.id.dNDEditComment);
    CheckBox chkFlag = findViewById(R.id.dNDCheckFlag);
    EditText editRung = findViewById(R.id.dNDEditRung);

    editWeight.setText(String.format("%.2f", dataDay.getWeight()));
    editComment.setText(dataDay.comment);
    chkFlag.setChecked(dataDay.flag);
    editRung.setText(String.format("%d", dataDay.rung));

    SimpleDateFormat sdfDay = new SimpleDateFormat(c.getString(R.string.LocalizedDate));
    Calendar tmpDate = new GregorianCalendar(this.mYear, this.mMonth, this.mDay);
    ((TextView) findViewById(R.id.dialogNewDataTitle)).setText(sdfDay.format(tmpDate.getTime()));
  }
}