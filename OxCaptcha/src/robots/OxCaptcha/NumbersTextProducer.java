/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package robots.OxCaptcha;

public class NumbersTextProducer implements TextProducer {

    private static final int DEFAULT_LENGTH = 5;
    private static final char[] NUMBERS = { '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9' };

    private final TextProducer _txtProd;
    
    public NumbersTextProducer() {
        this(DEFAULT_LENGTH);
    }
    
    public NumbersTextProducer(int length) {
        _txtProd = new DefaultTextProducer(length, NUMBERS);
    }

    @Override public String getText() {
        return new StringBuffer(_txtProd.getText()).toString();
    }
}
