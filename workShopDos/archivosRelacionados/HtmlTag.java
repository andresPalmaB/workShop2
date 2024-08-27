package workShopDos.archivosRelacionados;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/*
 * SD2x Homework #2
 * This class represents a single HTML tag.
 * Please do not change this code! Your solution will be evaluated using this version of the class.
 */

public class HtmlTag {

    protected final String element;
    protected final boolean openTag;
    
    public HtmlTag(String element, boolean isOpenTag) {
        this.element = element.toLowerCase();
        openTag = isOpenTag;
    }
    
    public String getElement() {
        return element;
    }

    // Determina si la etiqueta esta abierta
    public boolean isOpenTag() {
   	    return openTag && !isSelfClosing();
    }

    // comparar dos objetos de tipo HtmlTag y determinar si "coinciden"
    public boolean matches(HtmlTag other) {
        return other != null 
        	&& element.equalsIgnoreCase(other.element)
        	&& openTag != other.openTag;
    }

    // Retorna true si la etiqueta se cierra a si misma
    public boolean isSelfClosing() {
        return SELF_CLOSING_TAGS.contains(element);
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof HtmlTag) {
            HtmlTag other = (HtmlTag) obj;
            return element.equals(other.element)
            	&& openTag == other.openTag;
        }
        return false;
    }
    
    public String toString() {
        return "<" +
               (openTag ? "" : "/") +
               (element.equals("!--") ? "!-- --" : element) +
               ">";
    }
    
    /**
     * The remaining fields and functions are related to HTML file parsing.
     */

    // a set of tags that don't need to be matched (self-closing)
    // conjunto de etiquetas en HTML que no requieren un cierre explícito
    protected static final Set<String> SELF_CLOSING_TAGS = new HashSet<String>(
            Arrays.asList("!doctype", "!--", "?xml", "xml", "area", "base",
                          "basefont", "br", "col", "frame", "hr", "img",
                          "input", "link", "meta", "param"));

    // Constante que contiene los espacios que pueden exitir en un archivo html tales como
    // " " espacio, "\f": Salto de Pagina, "\n": Salto de línea (nueva línea), "\r": Retorno de carro, "\t": Tabulación
    protected static final String WHITESPACE = " \f\n\r\t";

    public static Queue<HtmlTag> tokenize(String text) {
        StringBuffer buf = new StringBuffer(text); // Se transforma el parametro text a StringBuffer
        Queue<HtmlTag> queue = new LinkedList<HtmlTag>(); // se crea una cola

        HtmlTag nextTag = nextTag(buf); // se crea la primera tag que existe text y se actualiza el buf

        // nextTag != null, quiere decir que los procedimientos se haran hasta que la condicion se cumpla
        while (nextTag != null) {
            queue.add(nextTag); // agregar a la cola todas las tag que vaya encontrando
            nextTag = nextTag(buf); // se analiza la siguiente tag en buf
        }
        return queue;
    }

    protected static HtmlTag nextTag(StringBuffer buf) {
        int openBracket = buf.indexOf("<"); //Identifica el indice donde se encuentra este caracter
        int closeBracket = buf.indexOf(">"); //Identifica el indice donde se encuentra este caracter

        //Si openBracket es -1, quiere decir que ese caracter no se encuentra en la cadena
        //closeBracket < openBracket, quiere decir este caracter ">" se encuentra antes que este "<"
        if (openBracket >= 0 && closeBracket > openBracket) {

            // Su finalidad es saber si hay una etiqueta de tipo comentario: <!-- -->
        	int commentIndex = openBracket + 4; // Calcula el inicio del comentario

            // Se verifica que haya suficientes caracteres para contener la secuencia "!--"
            // Se verifica que los caracteres que esten sean iguales a "!--"
            if (commentIndex <= buf.length()
            		&& buf.substring(openBracket + 1, commentIndex).equals("!--")) {

                // Busca la secuencia de cierre del comentario (-->) empezando desde commentIndex
                closeBracket = buf.indexOf("-->", commentIndex);

                // closeBracket < 0 indica que no hay cierre de comentario
                if (closeBracket < 0) {
                    return null;

                // Si se encuentra, inserta un espacio en buf en la posición commentIndex
                // y ajusta closeBracket para apuntar al carácter después de -->
                } else {
                    buf.insert(commentIndex, " ");
                    closeBracket += 3;
                }
            }

            // Se saca una cadena de caracteres con lo que este dentro de "<>"
            // se eliminan los espacia que haya al principio y al final de la cadena
            String element = buf.substring(openBracket + 1, closeBracket).trim();

            // Se extrae el nombre de la etiqueta y elimina cualquier atributo o espacio en blanco asociado
            for (int i = 0; i < WHITESPACE.length(); i++) {
                int attributeIndex = element.indexOf(WHITESPACE.charAt(i));
                if (attributeIndex >= 0) {
                    element = element.substring(0, attributeIndex);
                    break; // esto no es del codigo original
                }
            }
            
            // determine whether opening or closing tag
            boolean isOpenTag = true;
            int checkForClosing = element.indexOf("/"); // Se obtiene el indice del "/"

            // checkForClosing == 0, quiere decir que es una etiqueta de cierre
            if (checkForClosing == 0) {
                isOpenTag = false; // variable utilizada para actualizar el estado de la etiqueda
                // elimina el primer carácter de '/' de element, dejando solo el nombre de la etiqueta para procesarlo.
                element = element.substring(1);
            }

            // elimina todos los caracteres en element que no sean letras, números, signo de exclamación o guion
            element = element.replaceAll("[^a-zA-Z0-9!-]+", "");

            // Elimina la etiqueda manejada limpiando el buffer para permitir la siguiente operación
            buf.delete(0, closeBracket + 1);

            // Se crea una nueva instancia de HtmlTag con el nombre de la etiqueta (element) y el indicador
            return new HtmlTag(element, isOpenTag);
        } else {
            return null;
        }
    }    
}