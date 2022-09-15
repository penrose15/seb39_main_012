import {createGlobalStyle} from 'styled-components'
import {reset} from './css'

export const GlobalStyle = createGlobalStyle`
  ${reset}
  html {
    font-size: 62.5%;
    box-sizing: border-box;
  }
  
`
