import { router } from '@kit.ArkUI';
import { StudentMain } from './StudentMain';


@Entry
@Component
struct StudentPortal {
  build() {
    Column() {
      StudentMain();
    }
  }
}