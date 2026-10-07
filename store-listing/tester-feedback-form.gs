/**
 * Creates the "Football Club Quiz – Tester feedback" Google Form and a response spreadsheet.
 *
 * 1. Open https://script.google.com → New project, paste this file, click Run (createFeedbackForm).
 * 2. Allow the permissions it asks for (it only creates a form and a sheet in your Drive).
 * 3. View → Logs (or Execution log) shows the link to share and the edit link.
 */
function createFeedbackForm() {
  var form = FormApp.create('Football Club Quiz – Tester feedback');
  form.setDescription(
    'Thanks for testing Football Club Quiz! This takes about 2 minutes. ' +
    'You can send it more than once – for example after finding a mistake.');
  form.setCollectEmail(false);
  form.setAllowResponseEdits(false);
  form.setLimitOneResponsePerUser(false);
  form.setProgressBar(false);
  form.setConfirmationMessage('Thanks a lot – your feedback really helps!');

  form.addCheckboxItem()
    .setTitle('What have you played?')
    .setChoiceValues(['Daily challenge', 'Classic', 'Survival', '2 players', 'Browsed the clubs list']);

  form.addScaleItem()
    .setTitle('Overall, how much do you like the app?')
    .setBounds(1, 5).setLabels('Not at all', 'Love it')
    .setRequired(true);

  form.addScaleItem()
    .setTitle('How easy is the app to use?')
    .setBounds(1, 5).setLabels('Confusing', 'Very easy');

  form.addMultipleChoiceItem()
    .setTitle('How do the questions feel?')
    .setChoiceValues(['Too easy', 'About right', 'Too hard']);

  form.addMultipleChoiceItem()
    .setTitle('How often do you open the app?')
    .setChoiceValues(['Every day', 'A few times a week', 'About once a week', 'Less often']);

  form.addParagraphTextItem()
    .setTitle('Did you find a wrong answer or out-of-date club info?')
    .setHelpText('Please name the club and the question, e.g. "Wrong manager for Burnley".');

  form.addParagraphTextItem()
    .setTitle('Did anything go wrong? (crash, freeze, layout or translation problems)')
    .setHelpText('What were you doing when it happened?');

  form.addParagraphTextItem()
    .setTitle('What do you like most?');

  form.addParagraphTextItem()
    .setTitle('What should be improved or added?');

  form.addMultipleChoiceItem()
    .setTitle('Would you recommend the app to a football fan?')
    .setChoiceValues(['Yes', 'Maybe', 'No']);

  form.addTextItem()
    .setTitle('Phone model (optional)')
    .setHelpText('e.g. Samsung Galaxy S23, Pixel 8');

  form.addTextItem()
    .setTitle('Your email (optional)')
    .setHelpText('Only if you are happy to be contacted about your feedback.');

  var sheet = SpreadsheetApp.create('Football Club Quiz – Tester feedback (responses)');
  form.setDestination(FormApp.DestinationType.SPREADSHEET, sheet.getId());

  Logger.log('Share this link with testers: ' + form.shortenFormUrl(form.getPublishedUrl()));
  Logger.log('Edit the form: ' + form.getEditUrl());
  Logger.log('Responses sheet: ' + sheet.getUrl());
}
